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
        val ldapFilter: (DataFetchingEnvironment) -> Filter?,
        name: String,
        description: String?,
        type: GraphQLInputType,
        defaultValue: Any?) : GraphQLArgument(name, description, type, defaultValue) {

    fun isSupplied(env: DataFetchingEnvironment) = env.arguments[name] is String

    fun createFilter(env: DataFetchingEnvironment) = ldapFilter(env)

    companion object {
        fun of(arg: GraphQLArgument, ldapFilter: (DataFetchingEnvironment) -> Filter?) =
                LdapFilterArgument({ env ->
                    if (env.arguments[arg.name] == null) null
                    else ldapFilter(env)
                }, arg.name, arg.description, arg.type, arg.defaultValue)

        fun of(arg: GraphQLArgument.Builder, ldapFilter: (DataFetchingEnvironment) -> Filter?) =
                of(arg.build(), ldapFilter)

        fun of(graphName: String, description: String?, ldapFilter: (DataFetchingEnvironment) -> Filter) =
                of(builderOf(graphName, description), ldapFilter)

        fun builderOf(name: String, description: String? = null): GraphQLArgument.Builder {
            return GraphQLArgument.newArgument()
                    .name(name)
                    .description(description)
                    .type(Scalars.GraphQLString)
                    .defaultValue(null)
        }

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