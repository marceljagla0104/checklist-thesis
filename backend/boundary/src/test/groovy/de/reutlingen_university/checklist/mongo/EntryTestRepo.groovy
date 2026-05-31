package de.reutlingen_university.checklist.mongo

import de.reutlingen_university.checklist.documentation.Entry
import de.reutlingen_university.checklist.documentation.EntryRepo
import de.reutlingen_university.checklist.operation.Element
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.util.stream.Collectors

class EntryTestRepo implements EntryRepo {

    Map<String, Entry> collection = new HashMap<>()

    @Override
    Mono<Entry> save(Entry entry) {
        collection.put(entry.id, entry)
        return Mono.just(entry)
    }

    @Override
    Flux<Entry> findByIds(Set<String> ids) {
        return Flux.fromIterable(collection.values().stream().filter { it.id in ids }.collect(Collectors.toList()))
    }

    @Override
    Mono<Void> removeById(String id) {
        collection.remove(id)
        return Mono.empty()
    }

    @Override
    Flux<Entry> findUnfinishedByDocumentationIds(List<String> documentationIds) {
        return Flux.fromIterable(collection.values()
                .stream()
                .filter { it.documentationId in documentationIds && !it.finishedAt }
                .collect(Collectors.toList()))
    }

    @Override
    Mono<Entry> getById(String entryId) {
        return Mono.just(collection.get(entryId))
    }

    @Override
    Mono<Entry> findByElementAndDocumentationId(String elementId, String documentationId) {
        return Mono.just(collection.values()
                .stream()
                .filter { it.elementId == elementId && it.documentationId == documentationId }
                .findFirst()
                .orElse(null))
    }
}
