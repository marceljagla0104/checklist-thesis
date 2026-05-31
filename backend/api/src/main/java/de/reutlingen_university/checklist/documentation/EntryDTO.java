package de.reutlingen_university.checklist.documentation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EntryDTO {

    String id;
    String elementId;
    String description;
    String textEvent;
    List<String> phrases;
    Long duration;
    Long calculatedDuration;
    Instant startedAt;
    Instant finishedAt;
    Instant createdAt;
}
