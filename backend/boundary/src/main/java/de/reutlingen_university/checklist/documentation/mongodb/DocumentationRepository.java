package de.reutlingen_university.checklist.documentation.mongodb;

import de.reutlingen_university.checklist.documentation.Documentation;
import de.reutlingen_university.checklist.documentation.DocumentationRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class DocumentationRepository implements DocumentationRepo {

    private final DocumentationMongoRepo repo;

    @Override
    public Mono<Documentation> save(Documentation documentation) {
        return repo.save(documentation);
    }

    @Override
    public Mono<Documentation> addEntry(String documentationId, String entryId) {

        return repo.findById(documentationId)
                .map(documentation -> {
                    documentation.getEntryIds().add(entryId);
                    return documentation;
                })
                .flatMap(repo::save);
    }

    @Override
    public Mono<Documentation> getById(String documentationId) {
        return repo.findById(documentationId);
    }

    @Override
    public Mono<Void> removeEntry(String documentationId, String id) {
        return repo.deleteElementIdFromDocumentation(documentationId, id);
    }

    @Override
    public Flux<Documentation> findByOperationIdAndRoomId(String operationId, String roomId) {
        return repo.findByOperationIdAndRoomId(operationId, roomId);
    }


    @Override
    public Flux<Documentation> getUnfinished() {
        return repo.findBySavedAtIsNull();
    }

    @Override
    public Flux<Documentation> findAll() {
        return repo.findAll();
    }
}
