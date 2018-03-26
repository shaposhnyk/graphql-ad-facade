package com.shaposhnyk.graphql.adfacade

import graphql.Scalars
import graphql.schema.*
import org.slf4j.LoggerFactory
import org.springframework.LdapDataEntry
import org.springframework.ldap.core.ContextMapper
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder

data class SchemaParameters(val attrName: String = "LDAPDisplayName",
                            val attrDescription: String = "adminDescription",
                            val dnNameInGraph: String = "dn",
                            val dnDescription: String = "Directory Node",
                            val stringTypes: List<String> = listOf("2.5.5.12", "2.5.5.6"),
                            val ignoreAttributesLdapFilter: String = "")

open class LdapSchemaEntityFactory(val ldap: LdapTemplate,
                                   val base: String,
                                   val ldapClassName: String,
                                   val graphName: String = ldapClassName,
                                   val config: SchemaParameters = defaultParams) : EntityFactory {

    override fun listFetcher(): (DataFetchingEnvironment) -> Any {
        return { env ->
            val selectedAttributes = env.selectionSet.get().keys.toTypedArray()
            ldap.search(LdapQueryBuilder.query()
                    .base(base)
                    .attributes(*selectedAttributes)
                    .countLimit(env.arguments["limit"] as Int)
                    .where("objectClass").`is`(ldapClassName)
                    , ldapMapper)
        }
    }

    override fun listFieldDefinition(fieldName: String, fetcher: (DataFetchingEnvironment) -> Any): GraphQLFieldDefinition.Builder {
        return GraphQLFieldDefinition.newFieldDefinition()
                .type(GraphQLList.list(GraphQLTypeReference(graphName)))
                .name(fieldName)
                .dataFetcher(fetcher)
                .argument(GraphQLArgument.newArgument()
                        .name("limit")
                        .type(Scalars.GraphQLInt)
                        .defaultValue(100))
    }

    override fun objectDefinition(): GraphQLObjectType {
        val classInfo: LdapDataEntry = findClassSchema(ldapClassName) ?: throw IllegalArgumentException("Root class ${ldapClassName} not be found")

        // create base object with DN field
        val builder = GraphQLObjectType.newObject()
                .name(graphName)
                .description(classInfo.getStringAttribute(config.attrDescription))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name(config.dnNameInGraph)
                        .type(Scalars.GraphQLString)
                        .dataFetcher { it.getSource<LdapDataEntry>().dn.toString() }
                        .description(config.dnDescription))

        // find class' attribute definitions and add them
        val attrs = findFieldDefinitions(classInfo, mutableListOf(ldapClassName.toLowerCase()))
                .map { it.build() }
                .distinctBy { it.name }
        attrs.forEach { builder.field(it) }

        log.info("Built '$graphName' object schema of ${attrs.size} fields")
        return builder.build()
    }

    /**
     * Find all field definitions for class and all its descendants
     */
    fun findFieldDefinitions(info: LdapDataEntry, visitedClasses: MutableList<String>): MutableList<GraphQLFieldDefinition.Builder> {
        val fields = findClassFieldDefinitions(info).toMutableList()
        log.debug("Found {} fields on {}", fields.size, info.getStringAttribute("cn"))

        val subClassName = info.getStringAttribute("subClassOf")
        if (visitedClasses.contains(subClassName.toLowerCase())) {
            return fields
        }
        val subClassInfo = findClassSchema(subClassName)
        if (subClassInfo != null) {
            visitedClasses.add(subClassName.toLowerCase())
            fields.addAll(findFieldDefinitions(subClassInfo, visitedClasses))
        }
        return fields
    }

    /**
     * Find fields from given class only
     */
    private fun findClassFieldDefinitions(info: LdapDataEntry): Set<GraphQLFieldDefinition.Builder> {
        val ldapName = info.getStringAttribute("cn")
        val attrNames = (info.getStringAttributes("mustContain")?.toList() ?: listOf<String>())
                .union(info.getStringAttributes("mayContain")?.toList() ?: listOf<String>())

        val attrs: List<LdapDataEntry> = findAttributeSchemas(ldapName, attrNames)

        // boolean
        val bools = attrs
                .filter { "2.5.5.8" == it.getStringAttribute("attributeSyntax") }
                .filter { "TRUE" == it.getStringAttribute("isSingleValued") }
                .map {
                    val name = it.getStringAttribute("lDAPDisplayName")
                    fieldDefinition(it).type(Scalars.GraphQLBoolean)
                            .dataFetcher { env -> "TRUE" == env.getSource<LdapDataEntry>().getStringAttribute(name) }
                }

        // integer
        val ints = attrs
                .filter { "2.5.5.9" == it.getStringAttribute("attributeSyntax") }
                .filter { "TRUE" == it.getStringAttribute("isSingleValued") }
                .map {
                    val name = it.getStringAttribute(config.attrName)
                    fieldDefinition(it).type(Scalars.GraphQLInt)
                            .dataFetcher { env -> env.getSource<LdapDataEntry>().getStringAttribute(name) }
                }

        // strings
        val strs = attrs
                .filter { config.stringTypes.contains(it.getStringAttribute("attributeSyntax")) }
                .filter { "TRUE" == it.getStringAttribute("isSingleValued") }
                .map {
                    val name = it.getStringAttribute(config.attrName)
                    fieldDefinition(it)
                            .dataFetcher { env -> env.getSource<LdapDataEntry>().getStringAttribute(name) }
                }

        // list of string
        val lists = attrs
                .filter { config.stringTypes.contains(it.getStringAttribute("attributeSyntax")) }
                .filter { "TRUE" != it.getStringAttribute("isSingleValued") }
                .map {
                    val name = it.getStringAttribute(config.attrName)
                    fieldDefinition(it).type(GraphQLList.list(Scalars.GraphQLString))
                            .dataFetcher { env -> env.getSource<LdapDataEntry>().getStringAttributes(name) }
                }

        return bools
                .union(ints)
                .union(strs)
                .union(lists)
    }

    fun fieldDefinition(attr: LdapDataEntry): GraphQLFieldDefinition.Builder {
        return GraphQLFieldDefinition.newFieldDefinition()
                .name(attr.getStringAttribute(config.attrName))
                .description(attr.getStringAttribute(config.attrDescription))
                .type(Scalars.GraphQLString)
    }

    fun findClassSchema(className: String): LdapDataEntry? {
        val res = ldap.search(
                LdapQueryBuilder.query()
                        .base("cn=Schema,cn=Configuration,dc=" + base.substringAfter(",dc="))
                        .where("cn").`is`(className)
                        .and("objectClass").`is`("classSchema")
                , ldapMapper
        )
        if (res.size > 1) {
            throw IllegalArgumentException("Too many '${className}' objects found: ${res}")
        } else if (res.isEmpty()) {
            return null
        }

        return res.first() as LdapDataEntry
    }

    fun findAttributeSchemas(ldapName: String, attrNames: Set<String>): List<LdapDataEntry> {
        var criteria = LdapQueryBuilder.query()
                .base("cn=Schema,cn=Configuration,dc=" + base.substringAfter(",dc="))
                .where("cn").`is`("cn")

        for (name in attrNames) {
            criteria = criteria.or("cn").`is`(name)
        }

        return ldap.search(criteria, ldapMapper)
    }

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)

        val defaultParams = SchemaParameters()

        val ldapMapper = ContextMapper<DirContextAdapter> { ctx -> ctx as DirContextAdapter }
    }
}
