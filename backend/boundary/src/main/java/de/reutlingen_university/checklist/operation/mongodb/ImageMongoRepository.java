package de.reutlingen_university.checklist.operation.mongodb;

import de.reutlingen_university.checklist.operation.meta.Image;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ImageMongoRepository extends ReactiveMongoRepository<Image, String> {
}
