package com.shaposhnyk.graphql.adfacade

import graphql.Scalars
import graphql.schema.*
import org.slf4j.LoggerFactory
import org.springframework.LdapDataEntry
import org.springframework.ldap.core.ContextMapper
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.LdapOperations
import org.springframework.ldap.filter.*
import javax.naming.directory.SearchControls

data class ADParams(val attrName: String = "lDAPDisplayName",
                    val attrDescription: String = "adminDescription",
                    val dnNameInGraph: String = "dn",
                    val dnDescription: String = "Directory Node",
                    val stringTypes: List<String> = listOf("2.5.5.12", "2.5.5.6", "2.5.5.1"),
                    val attributesToIgnore: String = "",
                    val addRfc822LocalPart: Boolean = false) {

    fun isString(attrSchema: LdapDataEntry): Boolean {
        return stringTypes.contains(attrSchema.getStringAttribute("attributeSyntax"))
    }

    fun isInt(attrSchema: LdapDataEntry): Boolean {
        return "2.5.5.9" == attrSchema.getStringAttribute("attributeSyntax")
    }

    fun isBool(attrSchema: LdapDataEntry): Boolean {
        return "2.5.5.8" == attrSchema.getStringAttribute("attributeSyntax")
    }
}

class ADEntityBuilder(val ldap: LdapOperations, // ldap template
                      val base: String, // ldap base
                      val ldapClassName: String, // ldap objectClass
                      val graphName: String = ldapClassName, // GQL object name
                      val params: ADParams = defaultParams,
                      val additionalFilter: String? = null, // additional ldap filter
                      private val initialSchema: IHierarichalSchemaAccessor? = null) : IEntityFactory {

    val schema = initialSchema ?: LdapHierarchicalSchemaAccessor(LdapSchemaAccessor.cachingOf(ldap, base))

    // root class schema
    val classInfo: LdapDataEntry = schema.findClassSchema(ldapClassName)
            ?: throw IllegalArgumentException("Root class ${ldapClassName} cannot be found")

    // filter to use if no arguments is supplied
    val defaultFilter = if (additionalFilter == null) EqualsFilter("objectClass", ldapClassName)
    else AndFilter().and(EqualsFilter("objectClass", ldapClassName)).and(HardcodedFilter(additionalFilter))

    val fieldFactory = SimpleFieldFactory(params)

    /**
     * Fetches list of objects
     */
    override fun fetchList(env: DataFetchingEnvironment): Iterable<Any> {
        val filter = createLdapFilter(env)

        val selectedAttributes = fieldFactory.requiredAttributes(env)
        val ctrls = SearchControls()
        ctrls.countLimit = (env.arguments["limit"] as Int).toLong()
        ctrls.returningAttributes = selectedAttributes
        ctrls.searchScope = SearchControls.SUBTREE_SCOPE

        log.debug("Searching for {} from {}", filter, base)
        return ldap.search(base, filter.encode(), ctrls, ldapMapper)
    }

    override fun objectDefinition(): GraphQLObjectType {
        // set of all attributeSchemas of all subClasses of current class
        val attrNames = attributeNamesToExpose()

        // create base object with DN field
        val builder = GraphQLObjectType.newObject()
                .name(graphName)
                .description(classInfo.getStringAttribute(params.attrDescription))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name(params.dnNameInGraph)
                        .type(Scalars.GraphQLString)
                        .dataFetcher { it.getSource<LdapDataEntry>().dn.toString() }
                        .description(params.dnDescription))

        // transform LDAP attributeSchema into GraphQLFieldDefinition
        schema.findAttributeSchemas(attrNames, params.attrName, params.attributesToIgnore)
                .flatMap { createFieldDefinitions(it) }
                .forEach { builder.field(it) }

        log.info("Built '$graphName' GraphQL Object with ${builder.build().fieldDefinitions} fields")
        return builder.build()
    }

    fun attributeNamesToExpose(): Iterable<String> {
        return if (!params.addRfc822LocalPart) schema.findAttributesOfAClass(classInfo)
        else (schema.findAttributesOfAClass(classInfo).union(schema.findAttributesOfAClass("rFC822LocalPart")))
                .distinct()
                .sorted()
    }

    /**
     * Creates LDAP Filter at runtime
     */
    private fun createLdapFilter(env: DataFetchingEnvironment): Filter {
        if (env.arguments.isEmpty() || (env.arguments.size == 1 && "limit" in env.arguments)) {
            return defaultFilter // return default filter if there is no filter arguments
        }

        var filter = AndFilter()
        filter.and(EqualsFilter("objectClass", ldapClassName))
        additionalFilter?.let { filter.and(HardcodedFilter(additionalFilter)) }

        // build ldap filter from graphQL arguments
        env.fieldDefinition.arguments
                .filterIsInstance<LdapFilterArgument>()
                .map { it.createFilter(env) }
                .filter { it != null }
                .forEach { filter.and(it) }

        return filter
    }

    override fun listFieldDefinition(fieldName: String, fetcher: (DataFetchingEnvironment) -> Any): GraphQLFieldDefinition.Builder {
        val fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
                .type(GraphQLList.list(GraphQLTypeReference(graphName)))
                .name(fieldName)
                .dataFetcher(fetcher)
                .argument(GraphQLArgument.newArgument()
                        .name("limit")
                        .type(Scalars.GraphQLInt)
                        .defaultValue(100))

        val attributesToFilterOn = schema.findAttributesOfAClass(classInfo)
        schema.findAttributeSchemas(attributesToFilterOn, params.attrName, params.attributesToIgnore)
                .flatMap { f -> createArgumentDefinition(f) }
                .forEach { fieldBuilder.argument(it) }

        return fieldBuilder
    }

    private fun createArgumentDefinition(attrSchema: LdapDataEntry): List<GraphQLArgument> {
        val f = attrSchema.getStringAttribute(params.attrName)
        if (params.isString(attrSchema)) {
            return listOf(LdapFilterArgument.ofEq(f),
                    LdapFilterArgument.ofNotEq(f),
                    LdapFilterArgument.ofLike(f))
        } else if (params.isBool(attrSchema)) {
            return listOf(LdapFilterArgument
                    .of(LdapFilterArgument.builderOf(f).type(Scalars.GraphQLBoolean)) { env ->
                        EqualsFilter(f, if (true == env.arguments[f]) "TRUE" else "FALSE")
                    })
        } else if (params.isInt(attrSchema)) {
            return listOf(LdapFilterArgument
                    .of(LdapFilterArgument.builderOf(f).type(Scalars.GraphQLInt)) { env ->
                        EqualsFilter(f, (env.arguments[f] as Int).toString())
                    }, LdapFilterArgument
                    .of(LdapFilterArgument.builderOf(f + "Gte").type(Scalars.GraphQLInt)) { env ->
                        GreaterThanOrEqualsFilter(f, (env.arguments[f] as Int).toString())
                    }, LdapFilterArgument
                    .of(LdapFilterArgument.builderOf(f + "Lte").type(Scalars.GraphQLInt)) { env ->
                        LessThanOrEqualsFilter(f, (env.arguments[f] as Int).toString())
                    })
        }
        log.warn("Unknown attribute type for argument: {}", attrSchema)
        return emptyList()
    }

    /**
     * Find fields from given class only
     */
    private fun createFieldDefinitions(attrSchema: LdapDataEntry): Set<GraphQLFieldDefinition> {
        if (!fieldFactory.hasValidGraphName(attrSchema)) {
            return emptySet()
        }

        if ("TRUE" == attrSchema.getStringAttribute("isSingleValued")) {
            if (params.isBool(attrSchema)) {
                return setOf(fieldFactory.boolDefinition(attrSchema))
            } else if (params.isInt(attrSchema)) {
                return setOf(fieldFactory.boolDefinition(attrSchema))
            } else if (params.isString(attrSchema)) {
                return setOf(fieldFactory.boolDefinition(attrSchema))
            }
            log.warn("Unknown attribute {} type: {}", attrSchema, attrSchema.getStringAttribute("attributeSyntax"))
            return emptySet()
        }

        // multivalued fields
        if (params.isString(attrSchema)) {
            return setOf(fieldFactory.listDefinition(attrSchema))
        }
        log.warn("Unknown attribute {} type (MV): {}", attrSchema, attrSchema.getStringAttribute("attributeSyntax"))
        return emptySet()
    }

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)

        val defaultParams = ADParams()

        val ldapMapper = ContextMapper<DirContextAdapter> { ctx -> ctx as DirContextAdapter }
    }
}
