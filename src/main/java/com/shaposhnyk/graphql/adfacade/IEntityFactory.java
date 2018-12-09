package com.shaposhnyk.graphql.adfacade;

import com.google.common.base.Function;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLType;

import javax.annotation.Nonnull;

public interface IEntityFactory<R> {
  /** GraphQL Object definition */
  @Nonnull
  GraphQLType getObjectDefinition();

  /**
   * Fetches values according to the parameters specified in env
   *
   * @return iterator from fetched values
   */
  @Nonnull
  Iterable<R> fetch(@Nonnull DataFetchingEnvironment env);

  /**
   * @param fieldName
   * @param fetcher
   * @return builder for a list of objects from factory
   */
  @Nonnull
  GraphQLFieldDefinition.Builder newListFieldDefinition(
      @Nonnull String fieldName, @Nonnull Function<DataFetchingEnvironment, Iterable<R>> fetcher);

  @Nonnull
  default GraphQLFieldDefinition.Builder newListFieldDefinition(@Nonnull String fieldName) {
    return newListFieldDefinition(fieldName, this::fetch);
  }
}
