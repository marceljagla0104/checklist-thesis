package de.reutlingen_university.checklist.documentation.mongodb;

import reactor.core.publisher.Mono;

public interface DocumentationMongoRepoCustom  {

    Mono<Void> deleteElementIdFromDocumentation(String documentationId, String id);
}
