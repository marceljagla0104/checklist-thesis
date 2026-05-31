package de.reutlingen_university.checklist.mongo

import de.reutlingen_university.checklist.ElementPhrase
import de.reutlingen_university.checklist.ElementPhraseRepo
import reactor.core.publisher.Flux

import java.util.stream.Collectors

class ElementPhraseTestRepo implements ElementPhraseRepo {

    Map<String, ElementPhrase> collection = new HashMap<>()

    @Override
    Flux<ElementPhrase> saveAll(List<ElementPhrase> relations) {
        relations.each { collection.put(it.getId(), it) }
        return Flux.fromIterable(relations)
    }

    @Override
    Flux<ElementPhrase> findByElementIdIn(List<String> elementIds) {
        return Flux.fromIterable(collection.values().stream()
                .filter(elementMetaData -> elementIds.contains(elementMetaData.elementId))
                .collect(Collectors.toList())
        )
    }
}
