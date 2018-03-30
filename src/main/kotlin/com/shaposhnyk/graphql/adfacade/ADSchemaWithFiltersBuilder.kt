package com.shaposhnyk.graphql.adfacade

import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLType
import org.slf4j.LoggerFactory
import org.springframework.ldap.filter.AndFilter
import org.springframework.ldap.filter.EqualsFilter
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

        val crtls = SearchControls()
        crtls.countLimit = (env.arguments["limit"] as Int).toLong()
        crtls.returningAttributes = selectedAttributes
        crtls.searchScope = SearchControls.SUBTREE_SCOPE

        log.debug("Searching for {} from {}", rootFilter.encode(), base)
        return ldap.search(base, rootFilter.encode(), crtls, ADSchemaBuilder.ldapMapper)
    }

    override fun listFieldDefinition(fieldName: String, fetcher: (DataFetchingEnvironment) -> Any): GraphQLFieldDefinition.Builder {
        val fieldBuilder = schemaBuilder.listFieldDefinition(fieldName, fetcher)

        (schema.findClassSchema(schemaBuilder.ldapClassName)?.let { schema.findAttributesOfAClass(it, mandatoryOnly = true) }
                ?: emptyList())
                .flatMap { f ->
                    listOf(LdapFilterArgument.ofEq(f),
                            LdapFilterArgument.ofNotEq(f),
                            LdapFilterArgument.ofLike(f))
                }.forEach { fieldBuilder.argument(it) }

        return fieldBuilder
    }

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)
    }
}