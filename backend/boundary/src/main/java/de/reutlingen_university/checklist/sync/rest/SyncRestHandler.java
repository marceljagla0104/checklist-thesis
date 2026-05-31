package de.reutlingen_university.checklist.sync.rest;

import de.reutlingen_university.checklist.operation.CallCirculatingReq;
import de.reutlingen_university.checklist.operation.ChangeTabReq;
import de.reutlingen_university.checklist.operation.FinishCirculatingTaskReq;
import de.reutlingen_university.checklist.operation.StartCirculatingTaskReq;
import de.reutlingen_university.checklist.sync.SyncFacade;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class SyncRestHandler {

    private final SyncFacade facade;

    public Mono<ServerResponse> startCirculatingTask(ServerRequest serverRequest) {
        return buildResponse(serverRequest.bodyToMono(StartCirculatingTaskReq.class)
                .flatMap(facade::startCirculatingTask));
    }

    public Mono<ServerResponse> callCirculating(ServerRequest serverRequest) {
        return buildResponse(serverRequest.bodyToMono(CallCirculatingReq.class)
                .flatMap(facade::callCirculating));
    }

    public Mono<ServerResponse> finishCirculatingTask(ServerRequest serverRequest) {
        return buildResponse(serverRequest.bodyToMono(FinishCirculatingTaskReq.class)
                .flatMap(facade::finishCirculatingTask));
    }

    public Mono<ServerResponse> getUnfinishedTasks(ServerRequest ignored) {
        return buildResponse(facade.getUnfinishedTasks()
                .collectList());
    }

    public Mono<ServerResponse> changeTab(ServerRequest serverRequest) {
        return buildResponse(serverRequest.bodyToMono(ChangeTabReq.class)
                .flatMap(facade::publishTabChange));
    }

    private <T> Mono<ServerResponse> buildResponse(Mono<T> mono) {
        return mono.flatMap(body -> ServerResponse.ok().bodyValue(body)
                .switchIfEmpty(ServerResponse.ok().build())
                .onErrorResume(err -> ServerResponse.badRequest().bodyValue(err.getMessage())));
    }
}
