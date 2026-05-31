package de.reutlingen_university.checklist.documentation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface EntryRepo {
    Mono<Entry> save(Entry entry);

    Flux<Entry> findByIds(Set<String> ids);

    Mono<Void> removeById(String id);

    Flux<Entry> findUnfinishedByDocumentationIds(List<String> documentationIds);

    Mono<Entry> getById(String entryId);

    Mono<Entry> findByElementAndDocumentationId(String elementId, String documentationId);
}
