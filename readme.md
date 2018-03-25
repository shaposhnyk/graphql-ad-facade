# Simple GraphQL facade for Active Directory
Simple GraphQL facade for Active Directory

## Motivations
- simplify access to AD from UI components

## Features
- automatic schema discovery
- simplified support filters via top level input arguments
- fullblown ldap filters support via FilterExpression object
- nested group support

## How to use
Specify your ldap access parameters in application.yml, then
```
$ mvn spring-boot:run
$ start http://localhost:9900/graphiql
```
