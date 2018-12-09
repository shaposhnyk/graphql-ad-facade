package com.shaposhnyk.graphql.adfacade

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.springframework.ldap.core.LdapOperations

class LdapSchemaAccessorTest {

    val ldap: LdapOperations = LdapOpMock(emptyList())

    @Test
    fun getSchemaBaseWorksWithUpperAndLowerCase() {
        val schema1 = LdapSchemaAccessor.of(ldap, "ou=some,DC=shaposhnyk,DC=COM")
        assertThat(schema1.schemaBase, equalTo("cn=Schema,cn=Configuration,dc=shaposhnyk,dc=com"))

        val schema2 = LdapSchemaAccessor.of(ldap, "ou=some,dc=Shaposhnyk,dc=com")
        assertThat(schema2.schemaBase, equalTo("cn=Schema,cn=Configuration,dc=shaposhnyk,dc=com"))
    }
}