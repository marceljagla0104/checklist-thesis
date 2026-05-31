package de.reutlingen_university.checklist.documentation;

import lombok.Value;

import java.time.Instant;

@Value
public class DocumentationListItemDTO {
    String id;
    String operationName;
    Instant createdAt;
}
