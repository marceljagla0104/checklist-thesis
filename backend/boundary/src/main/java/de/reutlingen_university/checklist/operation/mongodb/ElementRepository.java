package de.reutlingen_university.checklist.operation.mongodb;

import de.reutlingen_university.checklist.operation.Element;
import de.reutlingen_university.checklist.operation.ElementRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@AllArgsConstructor
public class ElementRepository implements ElementRepo {

    private final ElementMongoRepo repo;

    @Override
    public Flux<Element> saveAll(List<Element> elements) {
        return repo.saveAll(elements);
    }

    @Override
    public Flux<Element> findAllById(List<String> elementIds) {
        return repo.findAllById(elementIds);
    }

    @Override
    public Flux<Element> findAllByPathId(String pathId) {
        return repo.findAllByPathIdsContains(pathId)
                .sort((e1, e2) -> e2.getId().compareTo(e1.getId()));
    }
}
