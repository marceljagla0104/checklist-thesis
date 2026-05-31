package de.reutlingen_university.checklist.operation.mongodb;

import de.reutlingen_university.checklist.operation.meta.MetaData;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MetaDataMongoRepo extends ReactiveMongoRepository<MetaData, String> {

}
