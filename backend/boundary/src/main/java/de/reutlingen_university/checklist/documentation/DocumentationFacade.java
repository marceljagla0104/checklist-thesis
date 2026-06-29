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

    // Funktion für Steuerungs-Intents (VUI Integration)
    public Mono<String> executeControlAction(String documentationID, String roomId, CreateEntryReq req){
        System.out.println("[EXECUTECONTROLACTION DEBUG] Starte Kette fuer DocId. " + documentationID);

        return handler.getDocumentation(documentationID)
                .doOnNext(doc -> System.out.println("[EXECUTECONTROLACTION DEBUG] Doku gefunden: " + doc.getId()))
                .switchIfEmpty(Mono.defer(() -> {
                        System.out.println("[EXECUTECONTROLACTION DEBUG] Fehler: Doku mit ID " + documentationID + " nicht in DB gefunden!");
                        return Mono.error(new Exception("Dokumentation nicht gefunden"));
                }))
                .flatMap(doc -> { 
                        System.out.println("[EXECUTECONTROLACTION DEBUG] Rufe jetzt isActionAllowed auf...");
                        
                        return isActionAllowed(documentationID, doc.getOperationId(), req.getRole(), req.getIntent(), req.getTextEvent())
                                .flatMap(allowed -> { 
                                        if (!allowed){
                                                System.out.println("[GUARD] Zugriff verweigert: " + req.getRole() + " darf nicht " + req.getIntent() + " / " + req.getTextEvent() + " ausfuehren.");
                                                return Mono.error(new Exception("Berechtigung verweigert: Rolle "+ req.getRole() + " nicht autorisiert für diese Aktion."));
                                        }

                                        System.out.println("==============================================================");
                                        System.out.println("[DEBUG FUER DocumentationFacade] Funktion executeControlAction wurde erfolgreich getriggert!");
                                        System.out.println("DocumentationID: " + documentationID);
                                        System.out.println("RoomID: " + roomId);
                                        System.out.println("Intent: " + req.getIntent());
                                        System.out.println("ElementID: " + req.getElementId());
                                        System.out.println("Rolle: " +req.getRole());
                                        System.out.println("Sprach-Event/Text" + req.getTextEvent());
                                        System.out.println("==============================================================");

                                        String textEvent = req.getTextEvent();
                                        switch (textEvent) {
                                                case "ZOOM_CAMERA":
                                                        System.out.println("[BPMN ENGINE] Sprachbefehl 'ZOOM_CAMERA' erhalten fuer Doc-ID: " + documentationID);
                                                        performCameraAction();
                                                        return this.sessionService.emitMsg(new ChecklistWebsocketMsg(
                                                                WebsocketMessageType.ENTRY_UPDATED,
                                                                documentationID,
                                                                roomId,
                                                                Map.of("action", "ZOOM_CAMERA", "status", "SUCCESS", "message", "Kamera-Steuerung wurde aktiviert.")
                                                        )).thenReturn("CONTROL_SUCCESS");

                                                case "UPDATE_NAME":
                                                        System.out.println("[BPMN ENGINE] Sprachbefehl 'UPDATE_NAME' erhalten fuer Doc-ID: " + documentationID);
                                                        String patientName = req.getDescription() != null ? req.getDescription() : "Unbekannter Patient";
                                                        updatePatientName(documentationID, patientName);
                                                        return this.sessionService.emitMsg(new ChecklistWebsocketMsg(
                                                                WebsocketMessageType.ENTRY_UPDATED,
                                                                documentationID,
                                                                roomId,
                                                                Map.of("action", "UPDATE_NAME", "status", "SUCCESS", "patientName", patientName)
                                                        )).thenReturn("CONTROL_SUCCESS");

                                                case "NEXT_STEP":
                                                        System.out.println("[BPMN ENGINE] Sprachbefehl 'NEXT_STEP' erhalten. Lade echte DB-Eintraege fuer Fortschritts-Abgleich...");
                                                        
                                                        return operationHandler.getOperationView(doc.getOperationId())
                                                                .flatMap(opView -> handler.getEntries(doc.getEntryIds()).collectList()
                                                                        .flatMap(entries -> {
                                                                                // Extrahiert die echten BPMN-Element-IDs aus den geladenen Einträgen
                                                                                List<String> completedElementIds = entries.stream()
                                                                                        .map(Entry::getElementId)
                                                                                        .toList();

                                                                                // Erstellet die flache Liste aller BPMN-Schritte
                                                                                List<ElementView> allElements = opView.getSubprocesses().stream()
                                                                                        .flatMap(sub -> sub.getElements().stream())
                                                                                        .toList();
                                                                                
                                                                                // Findet den am weitesten fortgeschrittenen Index
                                                                                int lastCompletedIndex = -1;
                                                                                for (int i = 0; i < allElements.size(); i++) {
                                                                                        if (completedElementIds.contains(allElements.get(i).getId())) {
                                                                                                lastCompletedIndex = i;
                                                                                        }
                                                                                }
                                                                                
                                                                                // Bestimmet deterministisch das darauffolgende Element
                                                                                String activeElementId;
                                                                                if (lastCompletedIndex == -1) {
                                                                                        activeElementId = allElements.isEmpty() ? "FINISHED" : allElements.get(0).getId();
                                                                                } else if (lastCompletedIndex >= allElements.size() - 1) {
                                                                                        activeElementId = "FINISHED";
                                                                                } else {
                                                                                        activeElementId = allElements.get(lastCompletedIndex + 1).getId();
                                                                                }
                                                                                
                                                                                if ("FINISHED".equals(activeElementId)) {
                                                                                        System.out.println("[BPMN ENGINE] Warnung: Prozess bereits vollständig beendet.");
                                                                                        return Mono.just("CONTROL_ALREADY_FINISHED");
                                                                                }
                                                                                
                                                                                String stepName = allElements.stream()
                                                                                        .filter(el -> el.getId().equals(activeElementId))
                                                                                        .map(ElementView::getName) // Hier ggf. getName() oder getDescription()
                                                                                        .findFirst()
                                                                                        .orElse("Automatisch abgehakt via VUI");

                                                                                java.time.Instant currentTime = java.time.Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
                                                                                
                                                                                CreateEntryCmd cmd = new CreateEntryCmd(
                                                                                        roomId,
                                                                                        documentationID,
                                                                                        activeElementId,
                                                                                        stepName,
                                                                                        "NEXT_STEP (Rolle: " + req.getRole()+")",
                                                                                        currentTime,
                                                                                        currentTime
                                                                                );
                                                                                
                                                                                return handler.createOrUpdateEntry(cmd)
                                                                                        .flatMap(entry -> this.sessionService.emitMsg(buildEntryUpdatedMsg(entry, documentationID, roomId))
                                                                                                .thenReturn("CONTROL_SUCCESS"));
                                                                        })
                                                                )
                                                                .onErrorResume(err -> {
                                                                        System.err.println("[BPMN FEHLER] Fehler beim automatischen Weiterschalten: " + err.getMessage());
                                                                        return Mono.just("CONTROL_ERROR");
                                                                });
                                                case "ADD_COMMENT":
                                                        System.out.println("[BPMN ENGINE] Sprachbefehl 'ADD_COMMENT' (Diktatmodus) erhalten. Text: " + req.getDescription());
                                                        
                                                        return operationHandler.getOperationView(doc.getOperationId())
                                                                .flatMap(opView -> handler.getEntries(doc.getEntryIds()).collectList()
                                                                        .flatMap(entries -> {
                                                                                List<String> completedElementIds = entries.stream().map(Entry::getElementId).toList();
                                                                                List<ElementView> allElements = opView.getSubprocesses().stream().flatMap(sub -> sub.getElements().stream()).toList();
                                                                                
                                                                                int lastCompletedIndex = -1;
                                                                                for (int i = 0; i < allElements.size(); i++) {
                                                                                        if (completedElementIds.contains(allElements.get(i).getId())) {
                                                                                                lastCompletedIndex = i;
                                                                                        }
                                                                                }
                                                                                
                                                                                // Kommentar wird an den aktuellen Schritt angehängt
                                                                                String targetElementId = (lastCompletedIndex == -1) ? (allElements.isEmpty() ? "FINISHED" : allElements.get(0).getId()) : allElements.get(Math.min(lastCompletedIndex + 1, allElements.size() - 1)).getId();
                                                                                
                                                                                String stepName = allElements.stream()
                                                                                        .filter(el -> el.getId().equals(targetElementId))
                                                                                        .map(ElementView::getName) // Hier ggf. getName() oder getDescription()
                                                                                        .findFirst()
                                                                                        .orElse("Automatisch abgehakt via VUI");

                                                                                java.time.Instant currentTime = java.time.Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
                                                                                
                                                                                CreateEntryCmd cmd = new CreateEntryCmd(
                                                                                        roomId, documentationID, targetElementId,
                                                                                        "Diktat (" + req.getRole() + "): " + req.getDescription() + " für schritt: " + stepName, // Freitext in DB
                                                                                        "ADD_COMMENT", currentTime, null
                                                                                    );
                                                                                
                                                                                return handler.createOrUpdateEntry(cmd)
                                                                                        .flatMap(entry -> this.sessionService.emitMsg(buildEntryUpdatedMsg(entry, documentationID, roomId))
                                                                                                .thenReturn("CONTROL_SUCCESS"));
                                                                        })
                                                                );
                                                default:
                                                        System.out.println("Unbekannter Befehl: " + textEvent);
                                                        return Mono.just("CONTROL_UNKNOWN");
                                        }
                                }); 
                }); 
    }

    private void performCameraAction(){
        System.out.println("[ACTION] Kamera-Steuerung wurde aktiviert.");
    }

    private void updatePatientName(String docId, String name){
        System.out.println("[ACTION] Patient " + name + " in Dokumentation " + docId + " gespeichert.");
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
                        System.out.println("[GUARD] Pruefung erfolgreich: Zugriff ERLAUBT.");
                        return Mono.just(true);
                });
    }

    // Intelligente Last-Progress Erkennung für die kontextsensitive Sprachsteuerung
    private String getActiveElementId(OperationView opView, Documentation doc){
        List<ElementView> allElements = opView.getSubprocesses().stream()
                .flatMap(sub -> sub.getElements().stream())
                .toList();
        
        int lastCompletedIndex = -1;
        
        // Scannt die Liste und findet die am weitesten hinten liegende ID, die einen Haken hat
        for (int i = 0; i < allElements.size(); i++) {
            if (doc.getEntryIds().contains(allElements.get(i).getId())) {
                lastCompletedIndex = i;
            }
        }
        
        // Fallback: Wenn noch absolut gar nichts abgehakt wurde, nimm das allererste Element
        if (lastCompletedIndex == -1) {
            return allElements.isEmpty() ? "FINISHED" : allElements.get(0).getId();
        }
        
        // Wenn das am weitesten fortgeschrittene Element das Ende der Liste ist
        if (lastCompletedIndex >= allElements.size() - 1) {
            return "FINISHED";
        }
        
        // Nimmt das Element, das direkt auf den am weitesten fortgeschrittenen Haken folgt!
        return allElements.get(lastCompletedIndex + 1).getId();
    }
}
