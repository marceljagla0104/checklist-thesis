package de.reutlingen_university.checklist.operation.mongodb;

import de.reutlingen_university.checklist.operation.meta.MetaData;
import de.reutlingen_university.checklist.operation.meta.MetaDataRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@AllArgsConstructor
public class MetaDataRepository implements MetaDataRepo {

    private final MetaDataMongoRepo repo;

    @Override
    public Flux<MetaData> saveAll(List<MetaData> metaDataList) {
        return repo.saveAll(metaDataList);
    }

    @Override
    public Flux<MetaData> findAllByIds(List<String> metaDataId) {
        return repo.findAllById(metaDataId);
    }

    @Override
    public Mono<MetaData> findById(String metaDataId) {
        return repo.findById(metaDataId);
    }
}
