package de.reutlingen_university.checklist.operation.mongodb;

import de.reutlingen_university.checklist.operation.Subprocess;
import de.reutlingen_university.checklist.operation.SubprocessRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@AllArgsConstructor
public class SubprocessRepository implements SubprocessRepo {

    private final SubprocessMongoRepo repo;

    @Override
    public Flux<Subprocess> saveAll(List<Subprocess> subprocesses) {
        return repo.saveAll(subprocesses);
    }

    @Override
    public Flux<Subprocess> findAllById(List<String> ids) {
        return repo.findAllById(ids);
    }

    @Override
    public Mono<Subprocess> findById(String id) {
        return repo.findById(id);
    }

    @Override
    public Mono<Subprocess> save(Subprocess subprocess) {
        return repo.save(subprocess);
    }
}
