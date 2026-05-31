package de.reutlingen_university.checklist.mongo


import de.reutlingen_university.checklist.operation.Subprocess
import de.reutlingen_university.checklist.operation.SubprocessRepo
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class SubprocessTestRepo implements SubprocessRepo {

    Map<String, Subprocess> collection = new HashMap<>()

    @Override
    Flux<Subprocess> saveAll(List<Subprocess> subprocesses) {
        subprocesses.each { collection.put(it.id, it) }
        return Flux.fromIterable(subprocesses)
    }

    @Override
    Flux<Subprocess> findAllById(List<String> ids) {
        return Flux.fromIterable(ids.collect { collection[it] })
    }

    @Override
    Mono<Subprocess> findById(String id) {
        return Mono.justOrEmpty(collection[id])
    }

    @Override
    Mono<Subprocess> save(Subprocess subprocess) {
        collection.put(subprocess.id, subprocess)
        return Mono.just(subprocess)
    }

}
