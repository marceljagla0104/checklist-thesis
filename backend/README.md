# Checklist Backend

## Preconditions

- Java 17 is installed (e.g. from here https://adoptium.net/de/temurin/releases/?version=17)
- [MongoDB](https://www.mongodb.com/) is installed and running on localhost:27017
- [Gradle](https://gradle.org/) is installed

## Architecture

The code is divided into three layers:

**API:** Here you can find all the models needed for communication with the server. Like messages, requests or responses. 

**Boundary:** This layer holds all the interfaces. Like REST-interfaces, a WebSocket interface and interfaces to work with MongoDB.

**Logic:** Here you can find all the business logic. Like parsers, handlers that call the database and the database models.

Within the layers its divided further into different packages that can be seen as modules. The admin module writes or deletes operations. Also it lists documentations for the admin view. 
The documentation module is responsible for the documentation and it's entries. The documentation is also used for tracking sessions for the clients. 
The operation module is responsible for serving the operations and the single operation elements with their meta data.
The sync module handles the synchronization of the clients via WebSockets. 

## Commands

To build the code run ``gradle build``

To start a local server run ``gradle bootRun``

To build the code to a jar (needed for docker image) run ``gradle bootJar`` (it will be found in /boundary/build/libs/boundary-1.0.jar)

To run the tests use ``gradle test``

## Tipps

This is a [SpringBoot](https://docs.spring.io/spring-boot/) application. It is recommended to learn the basic concepts of the framework like services, components, beans or configurations. Also [lombok](https://www.baeldung.com/intro-to-project-lombok) annotations are used to make life easier.  

Use [mongosh](https://www.mongodb.com/docs/mongodb-shell/) or [compass](https://www.mongodb.com/products/tools/compass) for development with MongoDB

In this project Reactor is used for the reactive programming. A small intro can be found [here](https://www.baeldung.com/reactor-core) and the official reference guide is [here](https://projectreactor.io/docs/core/release/reference/). 
It is also very similar to the Java Stream API. An intro to that is [here](https://www.baeldung.com/java-8-streams).

Tests are written in [Groovy](https://www.baeldung.com/groovy-language). Not all is tested. 