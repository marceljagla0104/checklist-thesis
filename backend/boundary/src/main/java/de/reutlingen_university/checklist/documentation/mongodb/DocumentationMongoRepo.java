package de.reutlingen_university.checklist.documentation.mongodb;

import de.reutlingen_university.checklist.documentation.Documentation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface DocumentationMongoRepo
        extends ReactiveMongoRepository<Documentation, String>, DocumentationMongoRepoCustom {

    Flux<Documentation> findByOperationIdAndRoomId(String operationId, String roomId);

    Flux<Documentation> findBySavedAtIsNull();
}
