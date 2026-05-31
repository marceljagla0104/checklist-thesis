package de.reutlingen_university.checklist.documentation;

import lombok.Value;

import java.time.Instant;

@Value
public class CreateEntryCmd {

    String room;
    String documentationId;
    String elementId;
    String description;
    String textEvent;
    Instant startedAt;
    Instant finishedAt;
}
