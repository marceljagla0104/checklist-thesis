package de.reutlingen_university.checklist.operation;

import lombok.Value;

import java.time.Instant;

@Value
public class OperationsListItemDTO {

    String id;
    String name;
    Instant createdAt;
}
