package de.reutlingen_university.checklist.sync.websockets;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ReactiveWebSocketHandler implements WebSocketHandler {

    private final SessionService sessionService;

    @NonNull
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(sessionService.getResponseEmitter(session)
                        .onErrorResume(error -> Mono.error(new RuntimeException("Error occurred", error))))
                .doOnSubscribe(subscription -> System.out.println("Connection opened"))
                .doOnError(error -> System.err.println("Connection ended with error: " + error.getMessage()))
                .doOnTerminate(() -> sessionService.removeSession(session.getId()));
    }
}
