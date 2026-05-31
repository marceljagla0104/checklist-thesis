package de.reutlingen_university.checklist.documentation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentationRepo {
    Mono<Documentation> save(Documentation documentation);

    Mono<Documentation> addEntry(String documentationId, String entryId);

    Mono<Documentation> getById(String documentationId);

    Mono<Void> removeEntry(String documentationId, String id);

    Flux<Documentation> getUnfinished();

    Flux<Documentation> findByOperationIdAndRoomId(String operationId, String roomId);

    Flux<Documentation> findAll();
}
