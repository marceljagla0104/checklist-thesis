package de.reutlingen_university.checklist.sync;

import de.reutlingen_university.checklist.OperationHandler;
import de.reutlingen_university.checklist.circulating.CirculatingTaskDTO;
import de.reutlingen_university.checklist.documentation.CreateEntryCmd;
import de.reutlingen_university.checklist.documentation.DocumentationHandler;
import de.reutlingen_university.checklist.documentation.Entry;
import de.reutlingen_university.checklist.operation.*;
import de.reutlingen_university.checklist.websockets.ChecklistWebsocketMsg;
import de.reutlingen_university.checklist.sync.websockets.SessionService;
import de.reutlingen_university.checklist.websockets.WebsocketMessageType;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class SyncFacade {
    private final SessionService sessionService;

    private final OperationHandler checklistHandler;

    private final DocumentationHandler documentationHandler;

    // reserved task id for the call circulating button
    private static final String PREDEFINED_TASK_ID = "call-circulating";

    // A new circulating task is started by a client
    public Mono<Void> startCirculatingTask(StartCirculatingTaskReq req) {
        return this.checklistHandler.getElementsByPathId(req.getPathId())
                .concatMap(element -> {
                    Instant startedAt = Instant.now();
                    CreateEntryCmd cmd = new CreateEntryCmd(
                            req.getRoomId(),
                            req.getDocumentationId(),
                            element.getId(),
                            element.getName(),
                            null,
                            startedAt,
                            null
                    );

                    //todo when elementId + documentationId exists -> overwrite
                    return this.documentationHandler.createIgnoredEntry(cmd) // create entry that doesn't show in text documentation for tracking started task
                            .flatMap(entry -> {
                                Map<String, Object> payload = Map.of(
                                        "id", entry.getElementId(),
                                        "entryId", entry.getId(),
                                        "documentationId", entry.getDocumentationId(),
                                        "roomId", entry.getRoomId(),
                                        "description", entry.getDescription(),
                                        "startedAt", entry.getStartedAt().orElse(null)
                                );
                                ChecklistWebsocketMsg msg = ChecklistWebsocketMsg.circulatingMsg(
                                        WebsocketMessageType.CALL_CIRCULATING,
                                        req.getDocumentationId(),
                                        payload
                                );
                                //todo maybe do in one message
                                return this.sessionService.emitMsg(msg) // notify all circulating clients of task
                                        .then(this.sessionService.emitMsg(new ChecklistWebsocketMsg( // notify all clients in the same room of task start
                                                WebsocketMessageType.CIRCULATING_TASK_STARTED,
                                                req.getDocumentationId(),
                                                req.getRoomId(),
                                                payload
                                        )));

                            });
                })
                .then();
    }

    // special predefined circulating task that calls circulating to a room
    public Mono<Void> callCirculating(CallCirculatingReq req) {
        CreateEntryCmd cmd = new CreateEntryCmd(
                req.getRoomId(),
                req.getDocumentationId(),
                PREDEFINED_TASK_ID,
                req.getDescription(),
                null,
                Instant.now(),
                null
        );
        return this.documentationHandler.createIgnoredEntry(cmd)  // create entry that doesn't show in text documentation for tracking started task
                .flatMap(entry -> {
                    Map<String, Object> payload = Map.of(
                            "id", entry.getElementId(),
                            "entryId", entry.getId(),
                            "roomId", entry.getRoomId(),
                            "description", entry.getDescription(),
                            "startedAt", entry.getStartedAt().orElse(null)
                    );

                    ChecklistWebsocketMsg checklistWebsocketMsg = ChecklistWebsocketMsg.circulatingMsg(
                            WebsocketMessageType.CALL_CIRCULATING,
                            entry.getDocumentationId(),
                            payload
                    );
                    return this.sessionService.emitMsg(checklistWebsocketMsg) // notify all circulating clients of task
                            .then(this.sessionService.emitMsg(new ChecklistWebsocketMsg( // notify all clients in the same room of task start
                                    WebsocketMessageType.CIRCULATING_TASK_STARTED,
                                    req.getDocumentationId(),
                                    req.getRoomId(),
                                    payload
                            )));
                });
    }

    // A circulating task is finished by a circulating client
    public Mono<Void> finishCirculatingTask(FinishCirculatingTaskReq req) {
        return documentationHandler.finishEntry(req.getEntryId())
                .flatMap(entry -> {
                    Map<String, Object> payload = Map.of(
                            "room", entry.getRoomId(),
                            "id", entry.getElementId(),
                            "entryId", entry.getId(),
                            "finishedAt", Instant.now()
                    );

                    ChecklistWebsocketMsg msg = new ChecklistWebsocketMsg(
                            WebsocketMessageType.CIRCULATING_TASK_DONE,
                            req.getDocumentationId(),
                            req.getRoomId(),
                            payload
                    );
                    return sessionService.emitMsg(msg) // notify all circulating clients of task finish
                            .then(sessionService.emitMsg(ChecklistWebsocketMsg.circulatingMsg( // notify all clients in the same room of task finish
                                    WebsocketMessageType.CIRCULATING_TASK_DONE,
                                    req.getDocumentationId(),
                                    payload
                            )));
                });
    }


    // Get all unfinished circulating tasks for display in the circulating client
    public Flux<CirculatingTaskDTO> getUnfinishedTasks() {
        return this.documentationHandler.getUnfinishedEntries()
                .concatMap(entry -> {
                    if (entry.getElementId().equals(PREDEFINED_TASK_ID)) {
                        return getPredefinedCirculatingTask(entry);
                    }
                    return getCirculatingTaskFromEntry(entry);
                });
    }

    // Notify all clients in a room of a tab change
    public Mono<Void> publishTabChange(ChangeTabReq req) {
        if (req.getRoomId() == null || req.getDocumentationId() == null) {
            return Mono.empty();
        }
        Map<String, Object> payload = Map.of(
                "room", req.getRoomId(),
                "tabName", req.getTabName()
        );
        ChecklistWebsocketMsg msg = new ChecklistWebsocketMsg(
                WebsocketMessageType.CHANGE_TAB,
                req.getDocumentationId(),
                req.getRoomId(),
                payload
        );
        return this.sessionService.emitMsg(msg);
    }

    private Mono<CirculatingTaskDTO> getCirculatingTaskFromEntry(Entry entry) {
        return this.checklistHandler.getElements(List.of(entry.getElementId()))
                .filter(element -> element.getRoles().contains(Role.CIRCULATING))
                .next()
                .map(element -> new CirculatingTaskDTO(
                        entry.getId(),
                        entry.getDocumentationId(),
                        entry.getDescription(),
                        entry.getRoomId(),
                        entry.getStartedAt().orElse(null)
                ));
    }

    private static Mono<CirculatingTaskDTO> getPredefinedCirculatingTask(Entry entry) {
        return Mono.just(new CirculatingTaskDTO(
                entry.getId(),
                entry.getDocumentationId(),
                entry.getDescription(),
                entry.getRoomId(),
                entry.getStartedAt().orElse(null)
        ));
    }
}
