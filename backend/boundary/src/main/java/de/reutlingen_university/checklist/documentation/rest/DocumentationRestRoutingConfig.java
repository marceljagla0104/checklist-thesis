package de.reutlingen_university.checklist.documentation.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class DocumentationRestRoutingConfig {

    @Bean
    RouterFunction<ServerResponse> documentationRoutes(DocumentationRestHandler handler) {
        return RouterFunctions.route()
                .GET("/documentation/{documentationId}", handler::getDocumentation)
                .GET("/documentation/list-unfinished/{operationId}/room/{roomId}", handler::listUnfinished)
                .POST("/documentation/create", handler::createDocumentation)
                .POST("/documentation/save", handler::saveDocumentation)

                .POST("/documentation/{documentationId}/room/{roomId}/entry/create", handler::createEntry)
                .POST("/documentation/{documentationId}/room/{roomId}/entry/{elementId}/remove", handler::removeEntry)
                .GET("/documentation/{documentationId}/entry/{elementId}", handler::getEntry)

                .build();
    }

}
