package de.reutlingen_university.checklist.mongo


import de.reutlingen_university.checklist.operation.Operation
import de.reutlingen_university.checklist.operation.OperationRepo
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class OperationTestRepo implements OperationRepo {

    Map<String, Operation> collection = new HashMap<>()

    @Override
    Mono<Operation> save(Operation operation) {
        collection.put(operation.getId(), operation)
        return Mono.just(operation)
    }

    @Override
    Mono<Operation> findById(String id) {
        return Mono.just(collection.get(id))
    }

    @Override
    Flux<Operation> findAll() {
        return Flux.fromIterable(collection.values())
    }
}
