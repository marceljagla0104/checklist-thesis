package de.reutlingen_university.checklist.websockets;

import lombok.Value;

import java.util.Map;

@Value
public class ChecklistWebsocketMsg {
    WebsocketMessageType type;
    String documentationId;
    String roomId;
    Map<String, Object> payload;

    // specific room id that is shared by all circulating clients
    private static final String CIRCULATING_NURSE_ROOM_NAME = "CIRCULATING";

    public static ChecklistWebsocketMsg circulatingMsg(
            WebsocketMessageType type,
            String documentationId,
            Map<String, Object> payload
    ) {
        // documentation id is needed for callback
        return new ChecklistWebsocketMsg(type, documentationId, CIRCULATING_NURSE_ROOM_NAME, payload);
    }
}
