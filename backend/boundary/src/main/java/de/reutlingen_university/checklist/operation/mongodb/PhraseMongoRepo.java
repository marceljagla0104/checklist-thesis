package de.reutlingen_university.checklist.operation.mongodb;

import de.reutlingen_university.checklist.documentation.Phrase;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PhraseMongoRepo extends ReactiveMongoRepository<Phrase, String> {

}
