package com.shaposhnyk.graphql.adfacade

import org.slf4j.LoggerFactory
import org.springframework.LdapDataEntry
import java.util.*

class LdapHierarchicalSchemaAccessor(val ldap: ISchemaAccessor) : IHierarichalSchemaAccessor {

    private val allAttrs: List<String> = listOf("systemMustContain", "systemMayContain", "mustContain", "mayContain")
    private val mandatoryAttrs: List<String> = listOf("systemMustContain", "mustContain")
    private val optionalAttrs: List<String> = listOf("systemMayContain", "mayContain")
    private val classExpansionAttrs = listOf("subClassOf", "systemPossSuperiors", "systemAuxiliaryClass", "possSuperiors", "auxiliaryClass")

    /**
     * @return a list of classSchema objects which are subclasses of a given class or an ofEmpty list if class is not found
     */
    override fun findSubClassesOfAClass(className: String): List<LdapDataEntry> {
        return findClassSchema(className)?.let { findSubClassesOfAClass(it) } ?: emptyList()
    }

    /**
     * @return a list of classSchema objects which are subclasses of a given class or an ofEmpty list if class is not found
     */
    override fun findSubClassesOfAClass(classInfo: LdapDataEntry): List<LdapDataEntry> {
        val classNames = mutableSetOf<String>(ldapClassNameOf(classInfo))
        return findSubClassesOfAClass(classInfo, classNames)
    }

    private fun findSubClassesOfAClass(classInfo: LdapDataEntry, classNames: MutableSet<String>): List<LdapDataEntry> {
        var subClassInfos = getMergedValues(classInfo, classExpansionAttrs)
                .filter { className -> !classNames.contains(className.toLowerCase()) } // only from new classes
                .map { className -> findClassSchema(className) }
                .filterIsInstance<LdapDataEntry>()
                .toSet()

        if (subClassInfos.isEmpty()) {
            return emptyList()
        }

        for (subInfo in subClassInfos) { // side effect
            classNames.add(ldapClassNameOf(subInfo))
        }

        val resSubClasses = subClassInfos
                .flatMap { findSubClassesOfAClass(it, classNames) }

        return (subClassInfos.union(resSubClasses))
                .toList()
    }

    /**
     * @return a list of attribute names of a given class or an ofEmpty list if class is not found
     */
    override fun findAttributesOfAClass(className: String): List<String> {
        return findClassSchema(className)?.let { findAttributesOfAClass(it) } ?: emptyList()
    }

    /**
     * @return a list of attribute names of a given class
     */
    override fun findAttributesOfAClass(classInfo: LdapDataEntry, mandatoryOnly: Boolean): List<String> {
        val subClasses = findSubClassesOfAClass(classInfo)
        log.debug("found {} subclasses of a {} class", subClasses.size, classInfo)

        return (listOf(classInfo).union(subClasses))
                .flatMap { if (mandatoryOnly) getMandatoryAttributeNames(it) else getDefinedAttributeNames(it) }
                .distinct()
    }

    /**
     * @return normalized class name
     */
    private fun ldapClassNameOf(classInfo: LdapDataEntry): String {
        return classInfo.getStringAttribute("cn").toLowerCase(Locale.ENGLISH)
    }

    /**
     * @return all attribute names from must- and mayContain
     */
    fun getDefinedAttributeNames(classInfo: LdapDataEntry): List<String> {
        return getMergedValues(classInfo, allAttrs)
                .distinct()
    }

    /**
     * @return all mandatory attribute names
     */
    fun getMandatoryAttributeNames(classInfo: LdapDataEntry): List<String> {
        return getMergedValues(classInfo, mandatoryAttrs)
    }

    /**
     * @return all optional attribute names
     */
    fun getOptionalAttributeNames(classInfo: LdapDataEntry): List<String> {
        return getMergedValues(classInfo, optionalAttrs)
    }

    private fun getMergedValues(classInfo: LdapDataEntry, sourceAttributes: List<String>): List<String> {
        return sourceAttributes
                .map { subClassAttr -> classInfo.getStringAttributes(subClassAttr) }
                .filter { it != null }
                .flatMap { it.toList() }
    }

    override fun findClassSchema(className: String): LdapDataEntry? = ldap.findClassSchema(className)

    override fun findAttributeSchemas(attrNames: Iterable<String>, attrName: String, attributesToIgnore: String): List<LdapDataEntry> {
        return ldap.findAttributeSchemas(attrNames, attrName, attributesToIgnore)
    }

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)
    }
}
