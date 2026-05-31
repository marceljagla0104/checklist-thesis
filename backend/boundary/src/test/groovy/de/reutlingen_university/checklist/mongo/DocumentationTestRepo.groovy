package de.reutlingen_university.checklist.mongo

import de.reutlingen_university.checklist.documentation.Documentation
import de.reutlingen_university.checklist.documentation.DocumentationRepo
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.util.stream.Collectors

class DocumentationTestRepo implements DocumentationRepo {

    Map<String, Documentation> collection = new HashMap<>()

    @Override
    Mono<Documentation> save(Documentation documentation) {
        collection.put(documentation.id, documentation)
        return Mono.just(documentation)
    }

    @Override
    Mono<Documentation> addEntry(String documentationId, String entryId) {
        def documentation = collection.get(documentationId)
        documentation.entryIds.add(entryId)
        return save(documentation)
    }

    @Override
    Mono<Documentation> getById(String documentationId) {
        return Mono.just(collection.get(documentationId))
    }

    @Override
    Mono<Void> removeEntry(String documentationId, String id) {
        def documentation = collection.get(documentationId)
        documentation.entryIds.remove(id)
        return save(documentation).then()
    }

    @Override
    Flux<Documentation> getUnfinished() {
        return Flux.fromIterable(collection.values()
                .stream()
                .filter { it.savedAt == null }
                .collect(Collectors.toList()))
    }

    @Override
    Flux<Documentation> findByOperationIdAndRoomId(String operationId, String roomId) {
        return Flux.fromIterable(collection.values()
                .stream()
                .filter { it.operationId == operationId && it.roomId == roomId }
                .collect(Collectors.toList()))
    }

    @Override
    Flux<Documentation> findAll() {
        return Flux.fromIterable(collection.values())
    }
}
