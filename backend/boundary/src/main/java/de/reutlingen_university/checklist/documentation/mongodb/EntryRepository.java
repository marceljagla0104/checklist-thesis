package de.reutlingen_university.checklist.documentation.mongodb;

import de.reutlingen_university.checklist.documentation.Entry;
import de.reutlingen_university.checklist.documentation.EntryRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class EntryRepository implements EntryRepo {

    private final EntryMongoRepo repo;

    @Override
    public Mono<Entry> save(Entry entry) {
        return repo.save(entry);
    }

    @Override
    public Flux<Entry> findByIds(Set<String> ids) {
        return repo.findAllByIdIn(ids);
    }

    @Override
    public Mono<Void> removeById(String id) {
        return repo.removeById(id);
    }

    @Override
    public Flux<Entry> findUnfinishedByDocumentationIds(List<String> documentationIds) {
        return repo.findByFinishedAtIsNullAndDocumentationIdIn(documentationIds);
    }

    @Override
    public Mono<Entry> getById(String entryId) {
        return repo.getById(entryId);
    }

    @Override
    public Mono<Entry> findByElementAndDocumentationId(String elementId, String documentationId) {
        return repo.findByElementIdAndDocumentationId(elementId, documentationId);
    }

}
