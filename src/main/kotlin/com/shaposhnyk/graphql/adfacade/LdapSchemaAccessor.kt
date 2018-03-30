package com.shaposhnyk.graphql.adfacade

import org.springframework.LdapDataEntry
import org.springframework.ldap.core.ContextMapper
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.LdapOperations
import org.springframework.ldap.filter.*
import org.springframework.ldap.query.LdapQueryBuilder
import java.util.*

/**
 * Simplifies access to attributeSchema and classSchema AD objects
 */
class LdapSchemaAccessor(val ldap: LdapOperations, val schemaBase: String) : ISchemaAccessor {

    /**
     * @return classSchema for a given className
     * @throws IllegalArgumentException if more than one entry returned
     */
    override fun findClassSchema(className: String): LdapDataEntry? {
        val res = ldap.search(LdapQueryBuilder.query()
                .base(schemaBase)
                .where("cn").`is`(className)
                , ldapMapper)
        if (res.size > 1) {
            throw IllegalArgumentException("Too many '${className}' objects found: ${res}")
        } else if (res.isEmpty()) {
            return null
        }

        return res.first() as LdapDataEntry
    }

    /**
     * @return a list of attributeSchemas for a list of attribute names
     */
    override fun findAttributeSchemas(attrNames: Iterable<String>, attrName: String, attributesToIgnore: String): List<LdapDataEntry> {
        val namesFilter = OrFilter()
        for (name in attrNames) {
            namesFilter.or(EqualsFilter(attrName, name))
        }

        return ldap.search(schemaBase, allowedAttrsFilter(attributesToIgnore, namesFilter).encode(), ldapMapper)
    }

    private fun allowedAttrsFilter(attributesToIgnore: String, namesFilter: Filter): Filter {
        if (attributesToIgnore.isEmpty()) {
            return namesFilter
        }
        return AndFilter().appendAll(listOf(NotFilter(HardcodedFilter(attributesToIgnore)), namesFilter))
    }

    companion object {
        private val ldapMapper = ContextMapper<DirContextAdapter> { ctx -> ctx as DirContextAdapter }

        private val emptyAccessor = object : ISchemaAccessor {
            override fun findClassSchema(className: String): LdapDataEntry? = null

            override fun findAttributeSchemas(attrNames: Iterable<String>, attrName: String, attributesToIgnore: String): List<LdapDataEntry> = listOf()
        }

        /**
         * @return AD LDAP Schema Accessor
         */
        fun of(ldap: LdapOperations, base: String): LdapSchemaAccessor {
            val schemaBase = "cn=Schema,cn=Configuration,dc=" + base.toLowerCase(Locale.ENGLISH).substringAfter(",dc=")
            return LdapSchemaAccessor(ldap, schemaBase)
        }

        fun cachingOf(ldap: LdapOperations, base: String): ISchemaAccessor = cachingOf(of(ldap, base))

        fun cachingOf(accessor: ISchemaAccessor): ISchemaAccessor {
            val cachedSchema = SimpleCache(accessor::findClassSchema)
            return object : ISchemaAccessor {
                override fun findClassSchema(className: String): LdapDataEntry? {
                    return cachedSchema.get(className)
                }

                override fun findAttributeSchemas(attrNames: Iterable<String>, attrName: String, attributesToIgnore: String): List<LdapDataEntry> {
                    return accessor.findAttributeSchemas(attrNames, attrName, attributesToIgnore)
                }

            }
        }

        fun ofEmpty() = emptyAccessor
    }
}
