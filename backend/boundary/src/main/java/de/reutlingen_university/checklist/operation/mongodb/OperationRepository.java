package de.reutlingen_university.checklist.operation.mongodb;

import de.reutlingen_university.checklist.operation.Operation;
import de.reutlingen_university.checklist.operation.OperationRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class OperationRepository implements OperationRepo {

    private final OperationMongoRepo repo;

    @Override
    public Mono<Operation> save(Operation operation) {
        return repo.save(operation);
    }

    @Override
    public Mono<Operation> findById(String id) {
        return repo.findById(id);
    }

    @Override
    public Flux<Operation> findAll() {
        return repo.findAll();
    }
}
