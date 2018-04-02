package com.shaposhnyk.graphql.adfacade

import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLType

interface IEntityFactory {
    /**
     * GraphQL Object definition
     */
    fun objectDefinition(): GraphQLType

    fun listFieldDefinition(fieldName: String, fetcher: (DataFetchingEnvironment) -> Any): GraphQLFieldDefinition.Builder

    /**
     * Fetches a list of instances of given entity
     */
    fun fetchList(env: DataFetchingEnvironment): Iterable<Any>

    fun listFieldDefinition(fieldName: String) = listFieldDefinition(fieldName, this::fetchList)
}