package com.shaposhnyk.graphql.adfacade

import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLType

interface IEntityFactory {
    fun fetchList(env: DataFetchingEnvironment): Any

    fun objectDefinition(): GraphQLType

    fun listFieldDefinition(fieldName: String, fetcher: (DataFetchingEnvironment) -> Any): GraphQLFieldDefinition.Builder

    fun listFieldDefinition(fieldName: String) = listFieldDefinition(fieldName, this::fetchList)
}