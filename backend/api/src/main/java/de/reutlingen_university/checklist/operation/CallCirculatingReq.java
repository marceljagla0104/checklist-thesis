package de.reutlingen_university.checklist.operation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CallCirculatingReq {
    String roomId;
    String documentationId;
    String description;
}