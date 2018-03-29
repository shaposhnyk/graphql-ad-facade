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
                            val stringTypes: List<String> = listOf("2.5.5.12", "2.5.5.6", "2.5.5.1"),
                            val ignoreAttributesLdapFilter: String = "")

open class LdapSchemaEntityFactory(val ldap: LdapTemplate,
                                   val base: String,
                                   val ldapClassName: String,
                                   val graphName: String = ldapClassName,
                                   val config: SchemaParameters = defaultParams) : EntityFactory {

    private val classCache = SimpleCache({ className: String ->
        log.info("Searching class info: {}", className)
        ldap.search(LdapQueryBuilder.query()
                .base("cn=Schema,cn=Configuration,dc=" + base.substringAfter(",dc="))
                .where("cn").`is`(className)
                , ldapMapper)
    })

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
        val classInfo: LdapDataEntry = findClassSchema(ldapClassName) ?: throw IllegalArgumentException("Root class ${ldapClassName} cannot be found")

        // create base object with DN field
        val builder = GraphQLObjectType.newObject()
                .name(graphName)
                .description(classInfo.getStringAttribute(config.attrDescription))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name(config.dnNameInGraph)
                        .type(Scalars.GraphQLString)
                        .dataFetcher { it.getSource<LdapDataEntry>().dn.toString() }
                        .description(config.dnDescription))

        // set of all attributeSchemas of all subClasses of current class
        val attrNames = findAttrSchemaForAClass(classInfo)

        // transform LDAP attributeSchema into GraphQLFieldDefinition
        findAttributeSchemas(attrNames)
                .flatMap { createFieldDefinition(it) }
                .filter { it != null }
                .forEach { builder.field(it.build()) }

        log.info("Built '$graphName' GraphQL object schema of ${attrNames.size} fields")
        return builder.build()
    }

    fun findAttrSchemaForAClass(classInfo: LdapDataEntry): List<String> {
        val classNames = mutableSetOf<String>(classInfo.getStringAttribute("cn").toLowerCase())
        return findAttrSchemaForAClass(classInfo, classNames)
    }

    fun findAttrSchemaForAClass(classInfo: LdapDataEntry, classNames: MutableSet<String>): List<String> {
        var subClassInfos = listOf("subClassOf", "possSuperiors", "auxiliaryClass", "systemPossSuperiors", "systemAuxiliaryClass")
                .map { subClassAttr -> classInfo.getStringAttributes(subClassAttr) }
                .filter { it != null }
                .flatMap { it.toList().union(listOf("rFC822LocalPart")) }
                .filter { className -> !classNames.contains(className.toLowerCase()) } // only new classes
                .map { className -> findClassSchema(className) }
                .filterIsInstance<LdapDataEntry>()
                .toList()

        if (subClassInfos.isEmpty()) {
            return getMergedAttributeValues(classInfo)
        }

        for (subInfo in subClassInfos) { // side effect
            classNames.add(subInfo.getStringAttribute("cn").toLowerCase())
        }

        val subClassesAttrList = subClassInfos
                .flatMap { info -> getMergedAttributeValues(info) }
                .sorted()
                .distinct()
                .toList()

        val recursiveAttrList = subClassInfos
                .flatMap { findAttrSchemaForAClass(it, classNames) }

        return subClassesAttrList
                .union(recursiveAttrList)
                .filter { !it.isEmpty() }
                .filter { !it.contains("Exch") }
                .distinct()
                .sorted()
    }

    private fun getMergedAttributeValues(classInfo: LdapDataEntry): List<String> {
        return listOf("mustContain", "mayContain", "systemMustContain", "systemMayContain")
                .map { subClassAttr -> classInfo.getStringAttributes(subClassAttr) }
                .filter { it != null }
                .flatMap { it.toList() }
                .distinct()
                .toList()
    }

    /**
     * Find fields from given class only
     */
    private fun createFieldDefinition(attrSchema: LdapDataEntry): Set<GraphQLFieldDefinition.Builder> {
        val attrs = listOf(attrSchema)
        // boolean
        val bools = attrs
                .filter { "2.5.5.8" == it.getStringAttribute("attributeSyntax") }
                .filter { "TRUE" == it.getStringAttribute("isSingleValued") }
                .map {
                    val name = it.getStringAttribute(config.attrName)
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
                .name(attr.getStringAttribute(config.attrName).replace(Regex("[^a-zA-Z0-9]+"), ""))
                .description(attr.getStringAttribute(config.attrDescription))
                .type(Scalars.GraphQLString)
    }

    fun findClassSchema(className: String): LdapDataEntry? {
        val res = classCache.get(className)
        if (res.size > 1) {
            throw IllegalArgumentException("Too many '${className}' objects found: ${res}")
        } else if (res.isEmpty()) {
            return null
        }

        return res.first() as LdapDataEntry
    }

    fun findAttributeSchemas(attrNames: Collection<String>): List<LdapDataEntry> {
        var criteria = LdapQueryBuilder.query()
                .base("cn=Schema,cn=Configuration,dc=" + base.substringAfter(",dc="))
                .where("ldapDisplayName").`is`("cn")

        for (name in attrNames) {
            criteria = criteria.or("ldapDisplayName").`is`(name)
        }

        return ldap.search(criteria, ldapMapper)
    }

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)

        val defaultParams = SchemaParameters()

        val ldapMapper = ContextMapper<DirContextAdapter> { ctx -> ctx as DirContextAdapter }
    }
}
