package de.reutlingen_university.checklist;

import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

// Mongo configuration for reactive mongodb
@Configuration
@EnableMongoAuditing
@EnableReactiveMongoRepositories(basePackages = {
        "de.reutlingen_university.checklist.documentation.mongodb",
        "de.reutlingen_university.checklist.operation.mongodb",
},
        considerNestedRepositories = true)
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}") // value can be found in resources/application.properties
    String uri;

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate() {
        String databaseName = uri.substring(uri.lastIndexOf("/") + 1);
        return new ReactiveMongoTemplate(new SimpleReactiveMongoDatabaseFactory(
                MongoClients.create(uri),
                databaseName
        ));
    }
}