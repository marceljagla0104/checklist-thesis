package de.reutlingen_university.checklist.operation.meta;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MetaDataRepo {

    Flux<MetaData> saveAll(List<MetaData> metaData);

    Flux<MetaData> findAllByIds(List<String> metaDataId);

    Mono<MetaData> findById(String metaDataId);
}
