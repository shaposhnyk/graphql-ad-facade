package com.shaposhnyk.graphql.adfacade

import graphql.Scalars
import graphql.schema.*
import org.slf4j.LoggerFactory
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder


class LdapSchemaSimpleFiltersEntityFactory(
        ldap: LdapTemplate,
        base: String,
        ldapClassName: String,
        graphName: String = ldapClassName,
        config: SchemaParameters = LdapSchemaEntityFactory.defaultParams) : LdapSchemaEntityFactory(ldap, base, ldapClassName, graphName, config) {

    override fun listFetcher(): (DataFetchingEnvironment) -> Any {
        return { env ->
            val selectedAttributes = env.selectionSet.get().keys.toTypedArray()
            val filters = env.fieldDefinition.arguments
                    .filter { it is LdapFilterArgument && it.isSupplied(env) }
                    .map { (it as LdapFilterArgument).createFilter(env) }
                    .toList()

            ldap.search(LdapQueryBuilder.query()
                    .base(base)
                    .attributes(*selectedAttributes)
                    .countLimit(env.arguments["limit"] as Int)
                    .where("objectClass").`is`(ldapClassName)
                    , ldapMapper)
        }
    }

    override fun listFieldDefinition(fieldName: String, fetcher: (DataFetchingEnvironment) -> Any): GraphQLFieldDefinition.Builder {
        val classSchema = findClassSchema(ldapClassName)
        val filterNames = classSchema?.getStringAttributes("mustContain")?.toList() ?: listOf()

        val fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
                .type(GraphQLList.list(GraphQLTypeReference(graphName)))
                .name(fieldName)
                .dataFetcher(fetcher)
                .argument(GraphQLArgument.newArgument()
                        .name("limit")
                        .type(Scalars.GraphQLInt)
                        .defaultValue(100))

        filterNames
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