package de.reutlingen_university.checklist.documentation;

import de.reutlingen_university.checklist.OperationHandler;
import de.reutlingen_university.checklist.websockets.ChecklistWebsocketMsg;
import de.reutlingen_university.checklist.sync.websockets.SessionService;
import de.reutlingen_university.checklist.websockets.WebsocketMessageType;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class DocumentationFacade {

    private final DocumentationHandler handler;
    private final SessionService sessionService;
    private final OperationHandler operationHandler;

    // Returns a List of all unfinished documentations for the start view
    public Flux<DocumentationListItemDTO> listUnfinished(String operationId, String roomId) {
        return handler.listUnfinished(operationId, roomId)
                .flatMap(doc -> operationHandler.getOperationView(doc.getOperationId())
                        .map(op -> new DocumentationListItemDTO(doc.getId(), op.getName(), doc.getCreatedAt())));
    }

    // Creates a new documentation
    public Mono<String> createDocumentation(CreateDocumentationReq req) {
        CreateDocumentationCmd cmd = new CreateDocumentationCmd(req.getRoomId(), req.getOperationId());

        return handler.createDocumentation(cmd)
                .map(Documentation::getId);
    }

    // Creates a new entry or updates an existing one for a documentation id
    public Mono<String> createOrUpdateEntry(String documentationId, String roomId, CreateEntryReq req) {
        CreateEntryCmd cmd = new CreateEntryCmd(
                roomId,
                documentationId,
                req.getElementId(),
                req.getDescription(),
                req.getTextEvent(),
                req.getStartedAt(),
                req.getFinishedAt()
        );

        return handler.createOrUpdateEntry(cmd)
                .flatMap(entry -> this.sessionService.emitMsg(buildEntryUpdatedMsg(entry, documentationId, roomId)) // notify clients of the update
                        .thenReturn(entry.getId()));
    }


    // Returns a single entry from a documentation by its related element id
    public Mono<EntryDTO> getEntryByElementId(String documentationId, String elementId) {
        return handler.getEntryByElementId(documentationId, elementId)
                .map(entry -> toAPI(entry, List.of(), List.of()));
    }

    // Removes an entry from a documentation by its related element id
    public Mono<Entry> removeEntry(String documentationId, String elementId, String roomId) {
        return handler.getEntryByElementId(documentationId, elementId)
                .flatMap(entry -> handler.removeEntry(documentationId, entry.getId())
                        .thenReturn(entry))
                .flatMap(entry -> this.sessionService.emitMsg(buildEntryRemovedMsg(entry, documentationId, roomId))
                        .thenReturn(entry));
    }

    // Returns all entries for a documentation
    public Mono<DocumentationDTO> getDocumentation(String documentationId) {
        return handler.getDocumentation(documentationId)
                .flatMap(documentation -> handler.getEntries(documentation.getEntryIds())
                        .filter(entry -> !entry.isIgnore())
                        .collectList()
                        .flatMap(entries -> Flux.fromIterable(entries)
                                .concatMap(entry -> handler.getPhraseByElementId(entry.getElementId())
                                        .map(phrase -> toAPI(entry, entries, phrase.getDescriptions()))
                                        .defaultIfEmpty(toAPI(entry, entries, List.of())))
                                .collectList()
                                .map(entryDTOs -> new DocumentationDTO(
                                        documentation.getId(),
                                        entryDTOs
                                ))
                        ));
    }

    // Finishes a documentation
    public Mono<Void> saveDocumentation(SaveDocumentationReq saveDocumentationReq) {
        return handler.saveDocumentation(saveDocumentationReq.getDocumentationId(), saveDocumentationReq.getText());
    }

    // Helper method to build a websocket message for an entry removed event
    private ChecklistWebsocketMsg buildEntryRemovedMsg(Entry entry, String documentationId, String roomId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", entry.getElementId());
        payload.put("entryId", entry.getId());
        return new ChecklistWebsocketMsg(
                WebsocketMessageType.ENTRY_REMOVED,
                documentationId,
                roomId,
                payload
        );
    }

    // Helper method to build a websocket message for an entry updated event
    private static ChecklistWebsocketMsg buildEntryUpdatedMsg(Entry entry, String documentationId, String roomId) {
        // the payload contains the updated data that is needed to update the views
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", entry.getElementId());
        payload.put("entryId", entry.getId());
        Optional.ofNullable(entry.getDescription()).ifPresent(d -> payload.put("description", d));
        Optional.ofNullable(entry.getTextEvent()).ifPresent(t -> payload.put("textEvent", t));
        entry.getStartedAt().ifPresent(s -> payload.put("startedAt", s));
        entry.getFinishedAt().ifPresent(f -> payload.put("finishedAt", f));

        return new ChecklistWebsocketMsg(
                WebsocketMessageType.ENTRY_UPDATED,
                documentationId,
                roomId,
                payload
        );
    }

    // Helper method to convert an Entry to an EntryDTO
    private static EntryDTO toAPI(Entry entry, List<Entry> entries, List<String> phrases) {
        return new EntryDTO(
                entry.getId(),
                entry.getElementId(),
                entry.getDescription(),
                entry.getTextEvent(),
                phrases,
                entry.getDuration(entries),
                entry.getCalculatedDuration(entries),
                entry.getStartedAt().orElse(null),
                entry.getFinishedAt().orElse(null),
                entry.getCreatedAt()
        );
    }

    public Mono<EntryDTO> executeControlAction(String documentationID, String roomId, CreateEntryReq req){


        return Mono.empty();
    }
}
