package de.reutlingen_university.checklist.operation.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class OperationRestRoutingConfig {

    @Bean
    RouterFunction<ServerResponse> operationRoutes(OperationRestHandler handler) {
        return RouterFunctions.route()
                .GET("/operations", handler::getOperations)
                .GET("/operation/{operationId}", handler::getOperation)
                .GET("/image/{operationId}/{imageName}", handler::getImage) //todo simply use image id
                .build();
    }

}
