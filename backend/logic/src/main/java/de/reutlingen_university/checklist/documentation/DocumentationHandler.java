package de.reutlingen_university.checklist.documentation;

import de.reutlingen_university.checklist.ElementPhraseRepo;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class DocumentationHandler {

    private final EntryRepo entryRepo;
    private final DocumentationRepo docuRepo;
    private final ElementPhraseRepo elementPhraseRepo;
    private final PhraseRepo phraseRepo;

    public Flux<Documentation> listUnfinished(String operationId, String roomId) {
        return docuRepo.findByOperationIdAndRoomId(operationId, roomId)
                .filter(doc -> doc.getSavedAt() == null);
    }

    public Mono<Documentation> createDocumentation(CreateDocumentationCmd cmd) {
        Documentation documentation = Documentation.create(
                cmd.getOperationId(),
                cmd.getRoomId()
        );

        return docuRepo.save(documentation);
    }

    public Mono<Entry> createOrUpdateEntry(CreateEntryCmd cmd) {
        return getEntryByElementId(cmd.getDocumentationId(), cmd.getElementId())
                .map(e -> e.update(cmd.getDescription(), cmd.getTextEvent(), cmd.getStartedAt(), cmd.getFinishedAt())) // if entry exists update it
                .switchIfEmpty(Mono.just(Entry.create( // if entry doesn't exist create new one
                        cmd.getElementId(),
                        cmd.getDocumentationId(),
                        cmd.getRoom(),
                        cmd.getDescription(),
                        cmd.getStartedAt(),
                        cmd.getFinishedAt()
                )))
                .flatMap(entry -> entryRepo.save(entry)// save entry
                        .flatMap(e -> docuRepo.addEntry(cmd.getDocumentationId(), e.getId())) // add entry to documentation
                        .thenReturn(entry));
    }

    public Mono<Entry> createIgnoredEntry(CreateEntryCmd cmd) {
        Entry entry = Entry.createIgnored( // create ignored entry
                cmd.getElementId(),
                cmd.getDocumentationId(),
                cmd.getRoom(),
                cmd.getDescription(),
                cmd.getStartedAt(),
                cmd.getFinishedAt()
        );

        return entryRepo.findByElementAndDocumentationId(entry.getElementId(), entry.getDocumentationId()) // find entry with same element and documentation
                .map(e -> entry.withId(e.getId())) // if entry exists switch ignored entry id with existing entry id (overwrite)
                .switchIfEmpty(Mono.just(entry)) // else take ignored entry
                .flatMap(updated -> entryRepo.save(updated)
                        .flatMap(e -> docuRepo.addEntry(cmd.getDocumentationId(), e.getId()))
                        .thenReturn(updated));
    }

    public Mono<Entry> finishEntry(String entryId) {
        return entryRepo.getById(entryId)
                .map(e -> e.withFinishedAt(Instant.now()))
                .flatMap(entryRepo::save);
    }


    public Mono<Entry> getEntryByElementId(String documentationId, String elementId) {
        return docuRepo.getById(documentationId)
                .map(Documentation::getEntryIds)
                .flatMapMany(entryRepo::findByIds)
                .filter(entry -> entry.getElementId().equals(elementId))
                .singleOrEmpty();
    }

    public Mono<Void> removeEntry(String documentationId, String id) {
        return docuRepo.removeEntry(documentationId, id)
                .then(entryRepo.removeById(id));
    }

    public Mono<Documentation> getDocumentation(String documentationId) {
        return docuRepo.getById(documentationId);
    }

    public Flux<Entry> getEntries(Set<String> entryIds) {
        return entryRepo.findByIds(entryIds)
                .sort(Comparator.comparing(Entry::getCreatedAt));
    }

    public Mono<Phrase> getPhraseByElementId(String elementId) {
        return elementPhraseRepo.findByElementIdIn(List.of(elementId))
                .singleOrEmpty()
                .flatMap(temp -> phraseRepo.findById(temp.getPhraseId()));
    }

    public Flux<Entry> getUnfinishedEntries() {
        return docuRepo.getUnfinished()
                .map(Documentation::getId)
                .collectList()
                .flatMapMany(entryRepo::findUnfinishedByDocumentationIds)
                .sort((e1, e2) -> e2.getId().compareTo(e1.getId()));
    }

    public Mono<Void> saveDocumentation(String documentationId, String text) {
        return docuRepo.getById(documentationId)
                .map(doc -> doc.saveWithCustomText(text))
                .flatMap(docuRepo::save)
                .then();
    }

    public Flux<Documentation> listDocumentations() {
        return docuRepo.findAll();
    }
}
