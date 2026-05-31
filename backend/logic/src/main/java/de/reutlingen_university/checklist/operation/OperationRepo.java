package de.reutlingen_university.checklist.operation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OperationRepo {

    Mono<Operation> save(Operation operation);

    Mono<Operation> findById(String id);

    Flux<Operation> findAll();
}
