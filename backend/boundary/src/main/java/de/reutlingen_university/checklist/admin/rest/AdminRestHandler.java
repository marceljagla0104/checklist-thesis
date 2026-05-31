package de.reutlingen_university.checklist.admin.rest;

import de.reutlingen_university.checklist.admin.AdminFacade;
import lombok.AllArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class AdminRestHandler {

    private final AdminFacade facade;

    public Mono<ServerResponse> listDocumentations(ServerRequest serverRequest) {
        return buildResponse(facade.listDocumentations()
                .collectList());
    }

    public Mono<ServerResponse> createOperation(ServerRequest serverRequest) {
        Mono<FilePart> filePartMono = serverRequest.multipartData()
                .mapNotNull(multiValueMap -> multiValueMap.getFirst("file"))
                .filter(part -> part instanceof FilePart)
                .cast(FilePart.class);

        return buildResponse(filePartMono.flatMap(facade::createOperation));
    }

    public Mono<ServerResponse> deleteOperation(ServerRequest serverRequest) {
        String operationId = serverRequest.pathVariable("operationId");

        return buildResponse(facade.deleteOperation(operationId));
    }


    private <T> Mono<ServerResponse> buildResponse(Mono<T> mono) {
        return mono.flatMap(body -> ServerResponse.ok().bodyValue(body)
                .switchIfEmpty(ServerResponse.ok().build())
                .onErrorResume(err -> ServerResponse.badRequest().bodyValue(err.getMessage())));
    }


}
