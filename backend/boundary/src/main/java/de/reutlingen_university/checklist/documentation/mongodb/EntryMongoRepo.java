package de.reutlingen_university.checklist.documentation.mongodb;

import de.reutlingen_university.checklist.documentation.Entry;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface EntryMongoRepo extends ReactiveMongoRepository<Entry, String> {


    Flux<Entry> findAllByIdIn(Set<String> ids);

    Mono<Void> removeById(String id);

    Flux<Entry> findByFinishedAtIsNullAndDocumentationIdIn(List<String> documentationIds);

    Mono<Entry> getById(String entryId);

    Mono<Entry> findByElementIdAndDocumentationId(String elementId, String documentationId);
}
