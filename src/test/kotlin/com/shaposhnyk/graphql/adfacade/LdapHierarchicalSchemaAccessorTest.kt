package com.shaposhnyk.graphql.adfacade

import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.Test
import org.springframework.LdapDataEntry

class LdapHierarchicalSchemaAccessorTest {

    val ldap = LdapSchemaAccessor.ofEmpty()

    val schema = LdapHierarchicalSchemaAccessor(ldap)

    fun mapAccessor(map: Map<String, LdapDataEntry>) = object : ISchemaAccessor {
        override fun findClassSchema(className: String): LdapDataEntry? = map[className]


        override fun findAttributeSchemas(attrNames: Iterable<String>, attrName: String, attributesToIgnore: String): List<LdapDataEntry> {
            return attrNames
                    .map { attrName -> LdapMocks.emptyEntry("cn=$attrName") }
        }
    }

    @Test
    fun getDefinedAttributeNames() {
        val emptyClass = LdapMocks.emptyEntry("cn=some")
        assertThat(schema.getDefinedAttributeNames(emptyClass), hasSize(0))

        val singleValueClass = LdapMocks.singletonEntry("cn=some", "mayContain", "attr1")
        assertThat(schema.getDefinedAttributeNames(singleValueClass), hasItem("attr1"))
        assertThat(schema.getDefinedAttributeNames(singleValueClass), hasSize(1))

        val multiValuesClass = LdapMocks.singletonEntry("cn=some", "mayContain", listOf("attr1", "attr2"))
        assertThat(schema.getDefinedAttributeNames(multiValuesClass), hasItems("attr1", "attr2"))
        assertThat(schema.getDefinedAttributeNames(multiValuesClass), hasSize(2))

        val map2 = mapOf(Pair("mayContain", arrayOf("attr1")), Pair("mustContain", arrayOf("attr2", "attr3")))
        val multiValuesClass2 = LdapMocks.entry("cn=some", map2)
        assertThat(schema.getDefinedAttributeNames(multiValuesClass2), hasItems("attr1", "attr2", "attr3"))
        assertThat(schema.getDefinedAttributeNames(multiValuesClass2), hasSize(3))

        val map3 = mapOf(Pair("mayContain", arrayOf("attr1")), Pair("mustContain", arrayOf("attr1", "attr3")))
        val multiValuesClass3 = LdapMocks.entry("cn=some", map3)
        assertThat(schema.getDefinedAttributeNames(multiValuesClass3), hasItems("attr1", "attr3"))
        assertThat(schema.getDefinedAttributeNames(multiValuesClass3), hasSize(2))
    }

    @Test
    fun getMandatoryAttributeNames() {
        val map2 = mapOf(Pair("mayContain", arrayOf("attr1")), Pair("mustContain", arrayOf("attr2", "attr3")))
        val multiValuesClass2 = LdapMocks.entry("cn=some", map2)
        assertThat(schema.getMandatoryAttributeNames(multiValuesClass2), hasItems("attr2", "attr3"))
        assertThat(schema.getMandatoryAttributeNames(multiValuesClass2), hasSize(2))

    }

    @Test
    fun getOptionalAttributeNames() {
        val map2 = mapOf(Pair("mayContain", arrayOf("attr1")), Pair("mustContain", arrayOf("attr2", "attr3")))
        val multiValuesClass2 = LdapMocks.entry("cn=some", map2)

        assertThat(schema.getOptionalAttributeNames(multiValuesClass2), hasItems("attr1"))
        assertThat(schema.getOptionalAttributeNames(multiValuesClass2), hasSize(1))
    }

    @Test
    fun findSubClassesOfAClass() {
        val hierarchy = hierarchy(listOf("A", "B", "C", "D"))
        val schema = LdapHierarchicalSchemaAccessor(hierarchy)

        val subA = schema.findSubClassesOfAClass("A")
        assertThat(subA.map { it.toString() }, hasItems("cn=B", "cn=C", "cn=D"))
        assertThat(subA, hasSize(3))

        val subB = schema.findSubClassesOfAClass("B")
        assertThat(subB.map { it.toString() }, hasItems("cn=C", "cn=D"))
        assertThat(subB, hasSize(2))

        assertThat(schema.findSubClassesOfAClass("D"), hasSize(0))
        assertThat(schema.findSubClassesOfAClass("X"), hasSize(0))
    }

    @Test
    fun findSubClassesOfAClassWithLoops() {
        val hierarchy = recursiveHierarchy(listOf("A", "B", "C"))
        val schema = LdapHierarchicalSchemaAccessor(hierarchy)

        val subA = schema.findSubClassesOfAClass("A")
        assertThat(subA.map { it.toString() }, hasItems("cn=B", "cn=C"))
        assertThat(subA, hasSize(2))

        val subB = schema.findSubClassesOfAClass("B")
        assertThat(subB.map { it.toString() }, hasItems("cn=C", "cn=A"))
        assertThat(subB, hasSize(2))

        assertThat(schema.findSubClassesOfAClass("X"), hasSize(0))
    }

    @Test
    fun findAttributesOfAClass() {
        val hierarchy = hierarchy(listOf("A", "B", "C", "D"))
        val schema = LdapHierarchicalSchemaAccessor(hierarchy)

        val attrsA = schema.findAttributesOfAClass("A")
        assertThat(attrsA, hasItems("A1", "A2"))
        assertThat(attrsA, hasItems("B1", "B2"))
        assertThat(attrsA, hasItems("C1", "C2"))
        assertThat(attrsA, hasItems("D1", "D2"))
        assertThat(attrsA, hasSize(8))

        assertThat(schema.findAttributesOfAClass("D"), hasSize(2))
        assertThat(schema.findAttributesOfAClass("X"), hasSize(0))
    }

    private fun hierarchy(classNames: List<String>): ISchemaAccessor {
        val map = mutableMapOf<String, LdapDataEntry>()
        classNames
                .forEachIndexed { idx, n ->
                    val superClass = if (idx + 1 < classNames.size) classNames[idx + 1] else "top"
                    val attrMap = mapOf(Pair("mayContain", arrayOf("${n}1", "${n}2")),
                            Pair("subClassOf", arrayOf(superClass)),
                            Pair("cn", arrayOf(n)))
                    map[n] = LdapMocks.entry("cn=$n", attrMap)
                }
        return mapAccessor(map.toMap())
    }

    private fun recursiveHierarchy(classNames: List<String>): ISchemaAccessor {
        val map = mutableMapOf<String, LdapDataEntry>()
        classNames
                .forEachIndexed { idx, n ->
                    val superClass = classNames[(idx + 1) % classNames.size]
                    val attrMap = mapOf(Pair("mayContain", arrayOf("${n}1", "${n}2")),
                            Pair("subClassOf", arrayOf(superClass)),
                            Pair("cn", arrayOf(n)))
                    map[n] = LdapMocks.entry("cn=$n", attrMap)
                }
        return mapAccessor(map.toMap())
    }
}