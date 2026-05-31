package de.reutlingen_university.checklist.operation.mongodb;

import de.reutlingen_university.checklist.ElementPhrase;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ElementPhraseMongoRepo extends ReactiveMongoRepository<ElementPhrase, String> {
    Flux<ElementPhrase> findByElementIdIn(List<String> elementIds);
}
