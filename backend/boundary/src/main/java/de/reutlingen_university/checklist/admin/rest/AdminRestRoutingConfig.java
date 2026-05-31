package de.reutlingen_university.checklist.admin.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class AdminRestRoutingConfig {

    @Bean
    RouterFunction<ServerResponse> adminRoutes(AdminRestHandler handler) {
        return RouterFunctions.route()
                .POST("/operation", handler::createOperation)
                .POST("/operation/{operationId}/delete", handler::deleteOperation)

                .GET("/documentations", handler::listDocumentations)
                .build();
    }

}
