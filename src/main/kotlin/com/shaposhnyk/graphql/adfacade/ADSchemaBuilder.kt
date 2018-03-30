package com.shaposhnyk.graphql.adfacade

import graphql.Scalars
import graphql.schema.*
import org.slf4j.LoggerFactory
import org.springframework.LdapDataEntry
import org.springframework.ldap.core.ContextMapper
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.LdapOperations
import org.springframework.ldap.query.LdapQueryBuilder

data class ADParams(val attrName: String = "lDAPDisplayName",
                    val attrDescription: String = "adminDescription",
                    val dnNameInGraph: String = "dn",
                    val dnDescription: String = "Directory Node",
                    val stringTypes: List<String> = listOf("2.5.5.12", "2.5.5.6", "2.5.5.1"),
                    val attributesToIgnore: String = "",
                    val addRfc822LocalPart: Boolean = false)

class ADSchemaBuilder(val ldap: LdapOperations,
                      val base: String,
                      val ldapClassName: String,
                      val graphName: String = ldapClassName,
                      val params: ADParams = defaultParams,
                      private val initialSchema: IHierarichalSchemaAccessor? = null) : IEntityFactory {

    val schema = initialSchema ?: LdapHierarchicalSchemaAccessor(LdapSchemaAccessor.cachingOf(ldap, base))

    override fun fetchList(env: DataFetchingEnvironment): Any {
        val selectedAttributes = env.selectionSet.get().keys.toTypedArray()
        return ldap.search(LdapQueryBuilder.query()
                .base(base)
                .attributes(*selectedAttributes)
                .countLimit(env.arguments["limit"] as Int)
                .where("objectClass").`is`(ldapClassName)
                , ldapMapper)
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
        val classInfo: LdapDataEntry = schema.findClassSchema(ldapClassName)
                ?: throw IllegalArgumentException("Root class ${ldapClassName} cannot be found")

        // create base object with DN field
        val builder = GraphQLObjectType.newObject()
                .name(graphName)
                .description(classInfo.getStringAttribute(params.attrDescription))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name(params.dnNameInGraph)
                        .type(Scalars.GraphQLString)
                        .dataFetcher { it.getSource<LdapDataEntry>().dn.toString() }
                        .description(params.dnDescription))

        // set of all attributeSchemas of all subClasses of current class
        val attrNames = if (!params.addRfc822LocalPart) schema.findAttributesOfAClass(classInfo)
        else (schema.findAttributesOfAClass(classInfo).union(schema.findAttributesOfAClass("rFC822LocalPart")))
                .distinct()
                .sorted()

        // transform LDAP attributeSchema into GraphQLFieldDefinition
        schema.findAttributeSchemas(attrNames, params.attrName, params.attributesToIgnore)
                .flatMap { createFieldDefinitions(it) }
                .forEach { builder.field(it.build()) }

        log.info("Built '$graphName' GraphQL Object with ${attrNames.size} fields")
        return builder.build()
    }

    /**
     * Find fields from given class only
     */
    private fun createFieldDefinitions(attrSchema: LdapDataEntry): Set<GraphQLFieldDefinition.Builder> {
        val attrs = listOf(attrSchema)
        // boolean
        val bools = attrs
                .filter { "2.5.5.8" == it.getStringAttribute("attributeSyntax") }
                .filter { "TRUE" == it.getStringAttribute("isSingleValued") }
                .map {
                    val name = it.getStringAttribute(params.attrName)
                    defaultDefinition(it).type(Scalars.GraphQLBoolean)
                            .dataFetcher { env -> "TRUE" == env.getSource<LdapDataEntry>().getStringAttribute(name) }
                }

        // integer
        val ints = attrs
                .filter { "2.5.5.9" == it.getStringAttribute("attributeSyntax") }
                .filter { "TRUE" == it.getStringAttribute("isSingleValued") }
                .map {
                    val name = it.getStringAttribute(params.attrName)
                    defaultDefinition(it).type(Scalars.GraphQLInt)
                            .dataFetcher { env -> env.getSource<LdapDataEntry>().getStringAttribute(name) }
                }

        // strings
        val strs = attrs
                .filter { params.stringTypes.contains(it.getStringAttribute("attributeSyntax")) }
                .filter { "TRUE" == it.getStringAttribute("isSingleValued") }
                .map {
                    val name = it.getStringAttribute(params.attrName)
                    defaultDefinition(it)
                            .dataFetcher { env -> env.getSource<LdapDataEntry>().getStringAttribute(name) }
                }

        // list of string
        val lists = attrs
                .filter { params.stringTypes.contains(it.getStringAttribute("attributeSyntax")) }
                .filter { "TRUE" != it.getStringAttribute("isSingleValued") }
                .map {
                    val name = it.getStringAttribute(params.attrName)
                    defaultDefinition(it)
                            .type(GraphQLList.list(Scalars.GraphQLString))
                            .dataFetcher { env -> env.getSource<LdapDataEntry>().getStringAttributes(name) }
                }

        return bools
                .union(ints)
                .union(strs)
                .union(lists)
    }

    /**
     * @return default definition of a field
     */
    private fun defaultDefinition(attr: LdapDataEntry): GraphQLFieldDefinition.Builder {
        return GraphQLFieldDefinition.newFieldDefinition()
                .name(attr.getStringAttribute(params.attrName).replace(Regex("[^a-zA-Z0-9]+"), ""))
                .description(attr.getStringAttribute(params.attrDescription))
                .type(Scalars.GraphQLString)
    }

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)

        val defaultParams = ADParams()

        val ldapMapper = ContextMapper<DirContextAdapter> { ctx -> ctx as DirContextAdapter }
    }
}
