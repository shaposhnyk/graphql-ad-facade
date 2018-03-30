package com.shaposhnyk.graphql.adfacade

import graphql.Scalars
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLType
import org.slf4j.LoggerFactory
import org.springframework.LdapDataEntry
import org.springframework.ldap.filter.AndFilter
import org.springframework.ldap.filter.EqualsFilter
import org.springframework.ldap.filter.GreaterThanOrEqualsFilter
import org.springframework.ldap.filter.LessThanOrEqualsFilter
import javax.naming.directory.SearchControls


class ADSchemaWithFiltersBuilder(val schemaBuilder: ADSchemaBuilder) : IEntityFactory {
    // shortcuts
    private val ldap = schemaBuilder.ldap
    private val base = schemaBuilder.base
    private val schema = schemaBuilder.schema

    override fun objectDefinition(): GraphQLType = schemaBuilder.objectDefinition()

    override fun fetchList(env: DataFetchingEnvironment): Any {
        val selectedAttributes = env.selectionSet.get().keys.toTypedArray()
        val filters = env.fieldDefinition.arguments
                .filter { it is LdapFilterArgument && it.isSupplied(env) }
                .map { (it as LdapFilterArgument).createFilter(env) }
                .toList()

        // build ldap filter from graphQL arguments
        var rootFilter = AndFilter()
        rootFilter.and(EqualsFilter("objectClass", schemaBuilder.ldapClassName))
        filters.forEach { rootFilter.and(it) }

        val ctrls = SearchControls()
        ctrls.countLimit = (env.arguments["limit"] as Int).toLong()
        ctrls.returningAttributes = selectedAttributes
        ctrls.searchScope = SearchControls.SUBTREE_SCOPE

        log.debug("Searching for {} from {}", rootFilter.encode(), base)
        return ldap.search(base, rootFilter.encode(), ctrls, ADSchemaBuilder.ldapMapper)
    }

    override fun listFieldDefinition(fieldName: String, fetcher: (DataFetchingEnvironment) -> Any): GraphQLFieldDefinition.Builder {
        val fieldBuilder = schemaBuilder.listFieldDefinition(fieldName, fetcher)

        val attributesToFilterOn = (schema.findClassSchema(schemaBuilder.ldapClassName)?.let { schema.findAttributesOfAClass(it, mandatoryOnly = true) }
                ?: emptyList())

        schema.findAttributeSchemas(attributesToFilterOn, schemaBuilder.params.attrName, schemaBuilder.params.attributesToIgnore)
                .flatMap { f -> createFilters(f) }
                .forEach { fieldBuilder.argument(it) }

        return fieldBuilder
    }

    private fun createFilters(attrSchema: LdapDataEntry): List<GraphQLArgument> {
        val f = attrSchema.getStringAttribute(schemaBuilder.params.attrName)
        if (schemaBuilder.params.isString(attrSchema)) {
            return listOf(LdapFilterArgument.ofEq(f),
                    LdapFilterArgument.ofNotEq(f),
                    LdapFilterArgument.ofLike(f))
        } else if (schemaBuilder.params.isBool(attrSchema)) {
            return listOf(LdapFilterArgument
                    .of(LdapFilterArgument.builderOf(f).type(Scalars.GraphQLBoolean)) { env ->
                        EqualsFilter(f, if (true == env.arguments[f]) "TRUE" else "FALSE")
                    })
        } else if (schemaBuilder.params.isInt(attrSchema)) {
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
        return emptyList()
    }

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)
    }
}
