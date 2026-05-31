package de.reutlingen_university.checklist.documentation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SaveDocumentationReq {

    String documentationId;
    String text;
}
