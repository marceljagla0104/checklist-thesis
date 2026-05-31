package de.reutlingen_university.checklist.operation;

import de.reutlingen_university.checklist.operation.meta.Image;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

public interface ImageRepo {

    Flux<Image> saveAll(List<Image> images);

    Flux<Image> findAllById(Set<String> imageIds);
}
