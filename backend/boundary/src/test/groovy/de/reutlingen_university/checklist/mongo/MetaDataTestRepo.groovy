package de.reutlingen_university.checklist.mongo


import de.reutlingen_university.checklist.operation.meta.MetaData
import de.reutlingen_university.checklist.operation.meta.MetaDataRepo
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class MetaDataTestRepo implements MetaDataRepo {

    Map<String, MetaData> collection = new HashMap<>()

    @Override
    Flux<MetaData> saveAll(List<MetaData> metaData) {
        metaData.each { collection.put(it.id, it) }
        return Flux.fromIterable(metaData)
    }

    @Override
    Flux<MetaData> findAllByIds(List<String> metaDataId) {
        return Flux.fromIterable(metaDataId.collect { collection[it] })
    }

    @Override
    Mono<MetaData> findById(String metaDataId) {
        return Mono.justOrEmpty(collection[metaDataId])
    }
}
