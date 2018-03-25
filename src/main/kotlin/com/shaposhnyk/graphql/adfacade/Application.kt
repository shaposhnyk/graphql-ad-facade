package com.shaposhnyk.graphql.adfacade

import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchema
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.ldap.core.LdapTemplate

@SpringBootApplication
class Application {

    @Bean
    fun init(ldapTemplate: LdapTemplate) = CommandLineRunner {
        println("Hello Boot")
    }

    @Bean
    fun schema(ldapTemplate: LdapTemplate): GraphQLSchema {
        return automaticSchema(ldapTemplate)
    }

    fun automaticSchema(ldap: LdapTemplate): GraphQLSchema {
        log.info("Building automatic schema from cn=schema,cn=configuration")
        val person: EntityFactory = LdapSchemaSimpleFiltersEntityFactory(ldap, "ou=people,dc=shaposhnyk,dc=com",
                ldapClassName = "organizationalPerson", graphName = "person")
        val room: EntityFactory = LdapSchemaEntityFactory(ldap, "ou=rooms,dc=shaposhnyk,dc=com", "meetingRoom")
        val group: EntityFactory = LdapSchemaEntityFactory(ldap, "ou=groups,dc=shaposhnyk,dc=com", "group")

        val queryBuilder = GraphQLObjectType.newObject().name("Query")
                .field(person.listFieldDefinition("searchPeople"))
                .field(room.listFieldDefinition("searchMeetingRooms"))
                .field(group.listFieldDefinition("searchGroups"))
                .description("Simple GraphQL Facade for Active Directory")
                .build()

        val schema = GraphQLSchema.newSchema()
                .query(queryBuilder)
                .build(mutableSetOf(person.objectDefinition(),
                        room.objectDefinition(),
                        group.objectDefinition())
                )
        log.info("Schema built")
        return schema
    }

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}


