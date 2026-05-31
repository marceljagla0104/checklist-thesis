package de.reutlingen_university.checklist.operation.rest;

import de.reutlingen_university.checklist.operation.OperationFacade;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class OperationRestHandler {

    private final OperationFacade facade;

    public Mono<ServerResponse> getOperations(ServerRequest ignoredRequest) {
        return buildResponse(facade.getOperations()
                .collectList());
    }

    public Mono<ServerResponse> getOperation(ServerRequest serverRequest) {
        String operationId = serverRequest.pathVariable("operationId");

        return buildResponse(facade.getOperation(operationId));
    }


    public Mono<ServerResponse> getImage(ServerRequest serverRequest) {
        String operationId = serverRequest.pathVariable("operationId");
        String imageName = serverRequest.pathVariable("imageName");

        return buildResponse(facade.getImage(operationId, imageName));
    }

    private <T> Mono<ServerResponse> buildResponse(Mono<T> mono) {
        return mono.flatMap(body -> ServerResponse.ok().bodyValue(body)
                .switchIfEmpty(ServerResponse.ok().build())
                .onErrorResume(err -> ServerResponse.badRequest().bodyValue(err.getMessage())));
    }
}
