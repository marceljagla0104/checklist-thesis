package de.reutlingen_university.checklist;

import reactor.core.publisher.Flux;

import java.util.List;

public interface ElementPhraseRepo {

    Flux<ElementPhrase> saveAll(List<ElementPhrase> relations);

    Flux<ElementPhrase> findByElementIdIn(List<String> elementIds);
}
