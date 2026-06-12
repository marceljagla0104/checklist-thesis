package de.reutlingen_university.checklist.documentation;

import de.reutlingen_university.checklist.OperationHandler;
import de.reutlingen_university.checklist.operation.OperationView;
import de.reutlingen_university.checklist.websockets.ChecklistWebsocketMsg;
import de.reutlingen_university.checklist.sync.websockets.SessionService;
import de.reutlingen_university.checklist.websockets.WebsocketMessageType;
import de.reutlingen_university.checklist.operation.ElementView;
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

    private final de.reutlingen_university.checklist.security.SecurityService securityService;

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
    //Testfunktion für Steuerungs-Intents
    public Mono<String> executeControlAction(String documentationID, String roomId, CreateEntryReq req){
        System.out.println("[EXECUTECONTROLACTION DEBUG] Starte Kette für DocId. " + documentationID);

        return handler.getDocumentation(documentationID)
                .doOnNext(doc -> System.out.println("[EXECUTECONTROLACTION DEBUG] Doku gefunden: " + doc.getId()))
                .switchIfEmpty(Mono.defer(() -> {
                        System.out.println("[EXECUTECONTROLACTION DEBUG] Fehler: DOku mit ID " + documentationID + " nicht in DB gefunden!");
                        return Mono.error(new Exception("Dokumentation nicht gefunden"));
                }))
                .flatMap(doc -> {
                        System.out.println("[EXECUTECONTROLACTION DEBUG] Rufe jetzt isActionAllowed auf...");
                        return isActionAllowed(documentationID, doc.getOperationId(), req.getRole(), req.getIntent(), req.getTextEvent()); 
                })
                .flatMap(allowed -> {
                        if (!allowed){
                                System.out.println("[GUARD] Zugriff verweigert: " + req.getRole() + " darf nicht " + req.getIntent() + " / " + req.getTextEvent() + " ausfuehren.");
                                return Mono.error(new Exception("Berechtigung verweigert: Rolle "+ req.getRole() + " nicht autorisiert für diese Aktion."));
                        }
                        //Konsolenausgaben für den Live-Test
                        System.out.println("==============================================================");
                        System.out.println("[DEBUG FUER DocumentationFacade] Funktion executeControlAction wurde erfolgreich getriggert!");
                        System.out.println("DocumentationID: " + documentationID);
                        System.out.println("RoomID: "+ req.getRoomId());
                        System.out.println("Role: "+req.getRole());
                        System.out.println("Intent: "+ req.getIntent());
                        System.out.println("ElementID: "+ req.getElementId());
                        System.out.println("Sprach-Event/Text: "+ req.getTextEvent());
                        System.out.println("==============================================================");

                        String textEvent = req.getTextEvent();
                        switch (textEvent) {
                                case "ZOOM_CAMERA":
                                        performCameraAction();
                                        return this.sessionService.emitMsg(new ChecklistWebsocketMsg(
                                                WebsocketMessageType.ENTRY_UPDATED,
                                                documentationID,
                                                roomId,
                                                Map.of("action", "ZOOM_CAMERA", "status", "SUCCESS", "message", "Kamera-Steuerung wurde aktiviert.")
                                        )).thenReturn("CONTROL_SUCCESS");
                                case "UPDATE_NAME":
                                        String patientName = req.getDescription() != null ? req.getDescription() : "Unbekannter Patient";
                                        updatePatientName(documentationID, patientName);
                                        return this.sessionService.emitMsg(new ChecklistWebsocketMsg(
                                                WebsocketMessageType.ENTRY_UPDATED,
                                                documentationID,
                                                roomId,
                                                Map.of("action", "UPDATE_NAME", "status", "SUCCESS", "patientName", patientName)
                                        )). thenReturn("CONTROL_SUCCESS");
                                        
                                default:
                                        System.out.println("Unbekannter Befehl: " + textEvent);
                                        return Mono.just("CONTROL_UNKNOWN");
                        }

                });
        

    }
    private void performCameraAction(){
        System.out.println("[ACTION] Kamera-Steuerung wurde aktiviert.");
    }
    private void updatePatientName(String docId, String name){
        System.out.println("[ACTION] Patient " + name + "in Dokumentation " + docId + "gespeichert.");
    }

    private Mono<Boolean> isActionAllowed(String documentationId, String operationId, String role, String intent, String action){
        if(!"CONTROL_ACTION".equals(intent)){
                return Mono.just(true);    
        }

        boolean isConfigAllowed = securityService.isActionAllowed(action, role);
        if (!isConfigAllowed) {
                System.out.println("[GUARD] Zugriff verweigert durch META.yaml: " + role + " darf nicht " + action);
                return Mono.just(false);
        }

        return operationHandler.getOperationView(operationId)
                .zipWith(handler.getDocumentation(documentationId))
                .map(tuple -> !"FINISHED".equals(getActiveElementId(tuple.getT1(), tuple.getT2())))
                .flatMap(isProcessAllowed -> {
                        if (!isProcessAllowed) {
                        System.out.println("[GUARD] Prozess-Check fehlgeschlagen (OP beendet).");
                        return Mono.just(false);    
                        } 
                        System.out.println("[GUARD] Prüfung erfolgreich: Zugriff ERLAUBT.");
                        return Mono.just(true);
                });
        }
        

    private String getActiveElementId(OperationView opView, Documentation doc){
        List<ElementView> allElements = opView.getSubprocesses().stream()
                .flatMap(sub -> sub.getElements().stream())
                .toList();
        for (ElementView element : allElements){
                
                if(!doc.getEntryIds().contains(element.getId())){
                        return element.getId();
                }
        }
        return "FINISHED";

    }
}
