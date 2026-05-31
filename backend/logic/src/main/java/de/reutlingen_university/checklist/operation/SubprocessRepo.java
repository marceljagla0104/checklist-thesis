package de.reutlingen_university.checklist.operation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SubprocessRepo {

    Flux<Subprocess> saveAll(List<Subprocess> subprocesses);

    Flux<Subprocess> findAllById(List<String> ids);

    Mono<Subprocess> findById(String id);

    Mono<Subprocess> save(Subprocess subprocess);
}
