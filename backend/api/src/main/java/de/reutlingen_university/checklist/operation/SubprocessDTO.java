package de.reutlingen_university.checklist.operation;

import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
public class SubprocessDTO {


    String id;

    String name;

    List<ElementDTO> elements;

    Instant createdAt;
}
