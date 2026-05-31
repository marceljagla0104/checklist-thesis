package de.reutlingen_university.checklist.mongo

import de.reutlingen_university.checklist.operation.Element
import de.reutlingen_university.checklist.operation.ElementRepo
import reactor.core.publisher.Flux

class ElementTestRepo implements ElementRepo {

    Map<String, Element> collection = new HashMap<>()

    @Override
    Flux<Element> saveAll(List<Element> elements) {
        elements.each { collection.put(it.getId(), it) }
        return Flux.fromIterable(elements)
    }

    @Override
    Flux<Element> findAllById(List<String> elementIds) {
        return Flux.fromIterable(elementIds.collect { collection[it] })
    }

    @Override
    Flux<Element> findAllByPathId(String pathId) {
        return Flux.fromIterable(collection.values().findAll { it.getPathIds().contains(pathId) })
    }
}
