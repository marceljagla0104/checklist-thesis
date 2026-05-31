package de.reutlingen_university.checklist.operation.mongodb;

import de.reutlingen_university.checklist.operation.Subprocess;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface SubprocessMongoRepo extends ReactiveMongoRepository<Subprocess, String> {

}
