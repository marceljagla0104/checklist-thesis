package de.reutlingen_university.checklist.circulating;

import lombok.Value;

import java.time.Instant;

@Value
public class CirculatingTaskDTO {

    String entryId;
    String documentationId;
    String description;
    String roomId;
    Instant startedAt;
}
