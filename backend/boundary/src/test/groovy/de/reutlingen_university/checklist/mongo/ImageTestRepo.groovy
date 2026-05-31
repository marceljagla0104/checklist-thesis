package de.reutlingen_university.checklist.mongo

import de.reutlingen_university.checklist.operation.ImageRepo
import de.reutlingen_university.checklist.operation.meta.Image
import reactor.core.publisher.Flux

class ImageTestRepo implements ImageRepo {

    Map<String, Image> collection = new HashMap<>()

    @Override
    Flux<Image> saveAll(List<Image> images) {
        images.each { collection[it.id] = it }
        return Flux.fromIterable(images)
    }

    @Override
    Flux<Image> findAllById(Set<String> imageIds) {
        return Flux.fromIterable(imageIds.collect { collection[it] })
    }
}
