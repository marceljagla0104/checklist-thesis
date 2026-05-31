package de.reutlingen_university.checklist.admin;

import lombok.Value;

import java.time.Instant;

@Value
public class DocumentationAdminDTO {
    String id;
    String operationId;
    String operationName;
    String roomId;
    Instant createdAt;
    Instant savedAt;
}
