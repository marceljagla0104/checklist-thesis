package de.reutlingen_university.checklist.operation;

import reactor.core.publisher.Flux;

import java.util.List;

public interface ElementRepo {

    Flux<Element> saveAll(List<Element> elements);

    Flux<Element> findAllById(List<String> elementIds);

    Flux<Element> findAllByPathId(String pathId);
}
