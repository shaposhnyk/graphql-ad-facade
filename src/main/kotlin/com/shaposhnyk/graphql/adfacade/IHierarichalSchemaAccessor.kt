package com.shaposhnyk.graphql.adfacade

import org.springframework.LdapDataEntry

interface IHierarichalSchemaAccessor : ISchemaAccessor {
    /**
     * @return a list of classSchema objects which are subclasses of a given class or an ofEmpty list if class is not found
     */
    fun findSubClassesOfAClass(className: String): List<LdapDataEntry> {
        return findClassSchema(className)?.let { findSubClassesOfAClass(it) } ?: emptyList()
    }

    /**
     * @return a list of attribute names of a given class or an ofEmpty list if class is not found
     */
    fun findAttributesOfAClass(className: String): List<String> {
        return findClassSchema(className)?.let { findAttributesOfAClass(it) } ?: emptyList()
    }

    /**
     * @return a list of classSchema objects which are subclasses of a given class or an ofEmpty list if class is not found
     */
    fun findSubClassesOfAClass(classInfo: LdapDataEntry): List<LdapDataEntry>

    /**
     * @return a list of attribute names of a given class
     */
    fun findAttributesOfAClass(classInfo: LdapDataEntry, mandatoryOnly: Boolean = false): List<String>
}
