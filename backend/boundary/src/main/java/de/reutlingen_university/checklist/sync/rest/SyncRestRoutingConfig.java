package de.reutlingen_university.checklist.sync.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class SyncRestRoutingConfig {

    @Bean
    RouterFunction<ServerResponse> syncRoutes(SyncRestHandler handler) {
        return RouterFunctions.route()
                .POST("/sync/tab", handler::changeTab)
                .POST("/sync/circulating/call", handler::callCirculating)
                .POST("/sync/circulating/task/start", handler::startCirculatingTask)
                .POST("/sync/circulating/task/finish", handler::finishCirculatingTask)
                .GET("/sync/circulating/tasks/unfinished", handler::getUnfinishedTasks)

                .build();
    }

}
