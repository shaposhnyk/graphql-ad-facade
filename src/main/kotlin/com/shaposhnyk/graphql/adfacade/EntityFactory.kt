package com.shaposhnyk.graphql.adfacade

import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLSchema
import graphql.schema.GraphQLType

interface EntityFactory {
    fun listFetcher(): (DataFetchingEnvironment) -> Any

    fun objectDefinition(): GraphQLType

    fun listFieldDefinition(fieldName: String, fetcher: (DataFetchingEnvironment) -> Any): GraphQLFieldDefinition.Builder

    fun listFieldDefinition(fieldName: String) = listFieldDefinition(fieldName, listFetcher())
}