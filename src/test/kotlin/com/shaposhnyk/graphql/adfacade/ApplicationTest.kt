package com.shaposhnyk.graphql.adfacade

import graphql.schema.GraphQLSchema
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner


@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner::class)
class ApplicationTest {

    @Autowired
    lateinit var schema: GraphQLSchema

    @Test
    fun testGetAllPersons() {
        assertThat(schema, not(nullValue()))
        assertThat(schema.queryType.description, containsString("Active Directory"))
    }
}
