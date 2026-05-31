package de.reutlingen_university.checklist.documentation.rest;

import de.reutlingen_university.checklist.documentation.CreateDocumentationReq;
import de.reutlingen_university.checklist.documentation.CreateEntryReq;
import de.reutlingen_university.checklist.documentation.DocumentationFacade;
import de.reutlingen_university.checklist.documentation.SaveDocumentationReq;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class DocumentationRestHandler {

    private final DocumentationFacade facade;

    public Mono<ServerResponse> createDocumentation(ServerRequest serverRequest) {

        return buildResponse(serverRequest.bodyToMono(CreateDocumentationReq.class)
                .flatMap(facade::createDocumentation));
    }

    public Mono<ServerResponse> createEntry(ServerRequest serverRequest) {
    String documentationId = serverRequest.pathVariable("documentationId");
    String roomId = serverRequest.pathVariable("roomId");

    return serverRequest.bodyToMono(CreateEntryReq.class)
        .flatMap(req -> {
            // Intent-Weiche: Unterscheidung zwischen Standard-Dokumentation und Steuerungsbefehl
            if ("CONTROL_ACTION".equalsIgnoreCase(req.getIntent())) {
                // AF-3: Delegierung an Steuerungs-Logik (muss in Facade implementiert werden)
                return facade.executeControlAction(documentationId, roomId, req);
            } else {
                // AF-3: Standard-Pfad: Dokumentations-Eintrag erstellen
                return facade.createOrUpdateEntry(documentationId, roomId, req);
            }
        })
        .flatMap(result -> ServerResponse.ok().bodyValue(result))
        .onErrorResume(err -> ServerResponse.badRequest().bodyValue("Fehler bei der Intent-Verarbeitung: " + err.getMessage()));
}

    public Mono<ServerResponse> listUnfinished(ServerRequest serverRequest) {
        String operationId = serverRequest.pathVariable("operationId");
        String roomId = serverRequest.pathVariable("roomId");

        return buildResponse(facade.listUnfinished(operationId, roomId)
                .collectList());
    }


    public Mono<ServerResponse> getEntry(ServerRequest serverRequest) {
        String documentationId = serverRequest.pathVariable("documentationId");
        String elementId = serverRequest.pathVariable("elementId");

        return buildResponse(facade.getEntryByElementId(documentationId, elementId));
    }

    public Mono<ServerResponse> removeEntry(ServerRequest serverRequest) {
        String documentationId = serverRequest.pathVariable("documentationId");
        String elementId = serverRequest.pathVariable("elementId");
        String roomId = serverRequest.pathVariable("roomId");

        return buildResponse(facade.removeEntry(documentationId, elementId, roomId));
    }

    public Mono<ServerResponse> getDocumentation(ServerRequest serverRequest) {
        String documentationId = serverRequest.pathVariable("documentationId");

        return buildResponse(facade.getDocumentation(documentationId));
    }

    public Mono<ServerResponse> saveDocumentation(ServerRequest serverRequest) {

        return buildResponse(serverRequest.bodyToMono(SaveDocumentationReq.class)
                .flatMap(facade::saveDocumentation));
    }
    
    private <T> Mono<ServerResponse> buildResponse(Mono<T> mono) {
        return mono.flatMap(body -> ServerResponse.ok().bodyValue(body)
                .switchIfEmpty(ServerResponse.ok().build())
                .onErrorResume(err -> ServerResponse.badRequest().bodyValue(err.getMessage())));
    }
}
