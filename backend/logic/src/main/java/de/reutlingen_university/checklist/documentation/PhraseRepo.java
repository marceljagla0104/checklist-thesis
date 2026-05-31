package de.reutlingen_university.checklist.documentation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PhraseRepo {

    Flux<Phrase> saveAll(List<Phrase> phrases);

    Flux<Phrase> findAllByIds(List<String> strings);

    Mono<Phrase> findById(String phraseId);
}
