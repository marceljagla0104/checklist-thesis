package de.reutlingen_university.checklist.operation.mongodb;

import de.reutlingen_university.checklist.documentation.Phrase;
import de.reutlingen_university.checklist.documentation.PhraseRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@AllArgsConstructor
public class PhraseRepository implements PhraseRepo {

    private final PhraseMongoRepo repo;

    @Override
    public Flux<Phrase> saveAll(List<Phrase> phrases) {
        return repo.saveAll(phrases);
    }

    @Override
    public Flux<Phrase> findAllByIds(List<String> ids) {
        return repo.findAllById(ids);
    }

    @Override
    public Mono<Phrase> findById(String phraseId) {
        return repo.findById(phraseId);
    }
}
