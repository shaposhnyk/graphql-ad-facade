package com.shaposhnyk.graphql.adfacade

import org.springframework.LdapDataEntry

interface ISchemaAccessor {
    /**
     * @return classSchema for a given className
     * @throws IllegalArgumentException if more than one entry returned
     */
    fun findClassSchema(className: String): LdapDataEntry?

    /**
     * @return a list of attributeSchemas for a list of attribute names
     */
    fun findAttributeSchemas(attrNames: Iterable<String>, attrName: String, attributesToIgnore: String): List<LdapDataEntry>
}
