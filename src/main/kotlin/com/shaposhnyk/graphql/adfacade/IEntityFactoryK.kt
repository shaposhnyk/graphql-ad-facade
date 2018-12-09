package com.shaposhnyk.graphql.adfacade

import com.google.common.base.Function
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition

interface IEntityFactoryK<R> : IEntityFactory<R> {
    fun newListFieldDefinitionK(name: String, fetcher: (DataFetchingEnvironment) -> Iterable<R>): GraphQLFieldDefinition.Builder

    override fun newListFieldDefinition(fieldName: String, fetcher: Function<DataFetchingEnvironment, MutableIterable<R>>): GraphQLFieldDefinition.Builder {
        return newListFieldDefinitionK(fieldName, fetcher = { fetcher.apply(it)!! })
    }
}