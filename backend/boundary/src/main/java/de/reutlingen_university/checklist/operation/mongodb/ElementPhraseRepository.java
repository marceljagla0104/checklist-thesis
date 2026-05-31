package de.reutlingen_university.checklist.operation.mongodb;

import de.reutlingen_university.checklist.ElementPhrase;
import de.reutlingen_university.checklist.ElementPhraseRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@AllArgsConstructor
public class ElementPhraseRepository implements ElementPhraseRepo {

    private final ElementPhraseMongoRepo repo;

    @Override
    public Flux<ElementPhrase> saveAll(List<ElementPhrase> elementPhrases) {
        return repo.saveAll(elementPhrases);
    }

    @Override
    public Flux<ElementPhrase> findByElementIdIn(List<String> elementIds) {
        return repo.findByElementIdIn(elementIds);
    }
}
