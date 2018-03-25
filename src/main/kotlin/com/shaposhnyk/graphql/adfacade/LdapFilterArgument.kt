package com.shaposhnyk.graphql.adfacade

import graphql.Scalars
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLInputType
import org.springframework.ldap.filter.EqualsFilter
import org.springframework.ldap.filter.Filter
import org.springframework.ldap.filter.LikeFilter
import org.springframework.ldap.filter.NotFilter

class LdapFilterArgument(
        val ldapFilter: (DataFetchingEnvironment) -> Filter,
        name: String,
        description: String?,
        type: GraphQLInputType,
        defaultValue: Any?) : GraphQLArgument(name, description, type, defaultValue) {

    fun isSupplied(env: DataFetchingEnvironment) = env.arguments[name] is String

    fun createFilter(env: DataFetchingEnvironment) = ldapFilter(env)

    companion object {
        fun of(ldapFilter: (DataFetchingEnvironment) -> Filter, arg: GraphQLArgument) = LdapFilterArgument(ldapFilter, arg.name, arg.description, arg.type, arg.defaultValue)
        fun of(ldapFilter: (DataFetchingEnvironment) -> Filter, arg: GraphQLArgument.Builder) = of(ldapFilter, arg.build())

        fun of(name: String, description: String?, ldapFilter: (DataFetchingEnvironment) -> Filter) = of(ldapFilter, GraphQLArgument.newArgument()
                .name(name)
                .description(description)
                .type(Scalars.GraphQLString)
                .defaultValue(null))

        fun ofEq(fieldName: String) = of(fieldName, "matching to ${fieldName}") { env ->
            EqualsFilter(fieldName, env.arguments[fieldName] as String)
        }

        fun ofNotEq(fieldName: String) = of("${fieldName}Not", "not matching to ${fieldName}") { env ->
            NotFilter(EqualsFilter(fieldName, env.arguments["${fieldName}Not"] as String))
        }

        fun ofLike(fieldName: String) = of("${fieldName}Like", "contains value in ${fieldName}") { env ->
            LikeFilter(fieldName, env.arguments["${fieldName}Like"] as String)
        }
    }

}