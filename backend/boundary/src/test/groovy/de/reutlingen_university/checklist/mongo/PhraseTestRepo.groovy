package de.reutlingen_university.checklist.mongo


import de.reutlingen_university.checklist.documentation.Phrase
import de.reutlingen_university.checklist.documentation.PhraseRepo
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class PhraseTestRepo implements PhraseRepo {

    Map<String, Phrase> collection = new HashMap<>()

    @Override
    Flux<Phrase> saveAll(List<Phrase> phrases) {
        phrases.each { collection.put(it.id, it) }
        return Flux.fromIterable(phrases)
    }

    @Override
    Flux<Phrase> findAllByIds(List<String> strings) {
        return Flux.fromIterable(strings.collect { collection[it] })
    }

    @Override
    Mono<Phrase> findById(String phraseId) {
        return Mono.justOrEmpty(collection[phraseId])
    }
}
