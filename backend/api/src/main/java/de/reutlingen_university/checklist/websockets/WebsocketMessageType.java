package de.reutlingen_university.checklist.websockets;

public enum WebsocketMessageType {
    CALL_CIRCULATING,
    CIRCULATING_TASK_STARTED,
    CIRCULATING_TASK_DONE,
    ENTRY_UPDATED,
    ENTRY_REMOVED,
    CHANGE_TAB
}
