package de.reutlingen_university.checklist.operation.mongodb;

import de.reutlingen_university.checklist.operation.Element;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ElementMongoRepo extends ReactiveMongoRepository<Element, String> {

    Flux<Void> deleteAllByIdIn(List<String> elementIds);

    Flux<Element> findAllByPathIdsContains(String pathId);
}
