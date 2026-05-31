package de.reutlingen_university.checklist.sync.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.reutlingen_university.checklist.websockets.ChecklistWebsocketMsg;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SessionService {

    private final ObjectMapper objectMapper;

    Map<String, Sinks.Many<ChecklistWebsocketMsg>> sessionEmitters = new HashMap<>();

    public SessionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Flux<WebSocketMessage> getResponseEmitter(WebSocketSession session) {
        Sinks.Many<ChecklistWebsocketMsg> emitter = Sinks.many().multicast().onBackpressureBuffer(100);
        sessionEmitters.put(session.getId(), emitter);

        return emitter.asFlux()
                .filter(s -> hasRoomName(s, session))
                .map(this::convertToJson)
                .map(session::textMessage)
                .doOnComplete(() -> {
                    System.out.println("removing session");
                    sessionEmitters.remove(session.getId());
                });
    }

    public Mono<Void> emitMsg(ChecklistWebsocketMsg msg) {
        return Mono.fromRunnable(() -> sessionEmitters.values().forEach(sink -> {
            synchronized (sink) {
                System.out.println("emitting msg: " + msg);
                sink.emitNext(msg, Sinks.EmitFailureHandler.FAIL_FAST);
            }
        }));
    }


    private boolean hasRoomName(ChecklistWebsocketMsg msg, WebSocketSession session) {
        String roomName = getRoomName(session);
        return msg.getRoomId().contains(roomName);
    }

    private static String getRoomName(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery();
        String room = null;

        if (query != null) {
            Pattern pattern = Pattern.compile("room=([^&]*)");
            Matcher matcher = pattern.matcher(query);
            if (matcher.find()) {
                room = matcher.group(1);
            }
        }

        return Optional.ofNullable(room)
                .orElseThrow();
    }

    private String convertToJson(ChecklistWebsocketMsg msg) {
        String jsonString = "";
        try {
            jsonString = objectMapper.writeValueAsString(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    public void removeSession(String id) {
        System.out.println("connection closed");
        sessionEmitters.remove(id);
    }
}
