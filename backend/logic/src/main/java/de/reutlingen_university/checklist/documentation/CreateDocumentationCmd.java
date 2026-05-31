package de.reutlingen_university.checklist.documentation;

import lombok.Value;

@Value
public class CreateDocumentationCmd {
    String roomId;
    String operationId;
}
