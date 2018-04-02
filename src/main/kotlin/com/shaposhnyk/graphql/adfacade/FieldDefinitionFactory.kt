package com.shaposhnyk.graphql.adfacade

import graphql.Scalars
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLList
import org.springframework.LdapDataEntry

interface FieldDefinitionFactory {
    /**
     * @return list of LDAP attributes requires to build all requested fields (at runtime)
     */
    fun requiredAttributes(env: DataFetchingEnvironment): Array<String>

    fun listDefinition(attr: LdapDataEntry, graphName: String? = null): GraphQLFieldDefinition
    fun strDefinition(attr: LdapDataEntry, graphName: String? = null): GraphQLFieldDefinition
    fun intDefinition(attr: LdapDataEntry, graphName: String? = null): GraphQLFieldDefinition
    fun boolDefinition(attr: LdapDataEntry, graphName: String? = null): GraphQLFieldDefinition
}

/**
 * This factory does not allow field mapping. LDAP field MUST be mapped on GraphQL field with the same name
 */
class SimpleFieldFactory(val params: ADParams) : FieldDefinitionFactory {

    override fun requiredAttributes(env: DataFetchingEnvironment): Array<String> {
        return env.selectionSet.get().keys.toTypedArray()
    }

    override fun boolDefinition(attr: LdapDataEntry, graphName: String?): GraphQLFieldDefinition {
        val name = attr.getStringAttribute(params.attrName)
        return defaultDefinition(attr, graphName ?: name)
                .type(Scalars.GraphQLBoolean)
                .dataFetcher { env -> "TRUE" == env.getSource<LdapDataEntry>().getStringAttribute(name) }
                .build()
    }

    override fun intDefinition(attr: LdapDataEntry, graphName: String?): GraphQLFieldDefinition {
        val name = attr.getStringAttribute(params.attrName)
        return defaultDefinition(attr, graphName ?: name)
                .type(Scalars.GraphQLInt)
                .dataFetcher { env -> Integer.valueOf(env.getSource<LdapDataEntry>().getStringAttribute(name)) }
                .build()
    }

    override fun strDefinition(attr: LdapDataEntry, graphName: String?): GraphQLFieldDefinition {
        val name = attr.getStringAttribute(params.attrName)
        return defaultDefinition(attr, graphName ?: name)
                .dataFetcher { env -> env.getSource<LdapDataEntry>().getStringAttribute(name) }
                .build()
    }

    override fun listDefinition(attr: LdapDataEntry, graphName: String?): GraphQLFieldDefinition {
        val name = attr.getStringAttribute(params.attrName)
        return defaultDefinition(attr, graphName ?: name)
                .type(GraphQLList.list(Scalars.GraphQLString))
                .dataFetcher { env -> listOf(env.getSource<LdapDataEntry>().getStringAttributes(name)) }
                .build()
    }

    /**
     * @return default field definition for a given attribute
     */
    private fun defaultDefinition(attr: LdapDataEntry, graphName: String): GraphQLFieldDefinition.Builder {
        return GraphQLFieldDefinition.newFieldDefinition()
                .name(graphName)
                .description(attr.getStringAttribute(params.attrDescription))
                .type(Scalars.GraphQLString)
    }

    private fun defaultDefinition(attr: LdapDataEntry): GraphQLFieldDefinition.Builder {
        val name = attr.getStringAttribute(params.attrName)
        return defaultDefinition(attr, name)
    }
}