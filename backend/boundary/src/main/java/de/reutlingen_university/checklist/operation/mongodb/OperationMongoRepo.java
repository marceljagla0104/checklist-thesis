package de.reutlingen_university.checklist.operation.mongodb;

import de.reutlingen_university.checklist.operation.Operation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface OperationMongoRepo extends ReactiveMongoRepository<Operation, String> {

}
