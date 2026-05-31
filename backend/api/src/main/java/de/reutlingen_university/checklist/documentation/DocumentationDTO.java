package de.reutlingen_university.checklist.documentation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentationDTO {

    String id;
    List<EntryDTO> entries;

}
