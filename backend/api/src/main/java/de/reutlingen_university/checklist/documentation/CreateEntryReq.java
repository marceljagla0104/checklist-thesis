package de.reutlingen_university.checklist.documentation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEntryReq {

    String elementId;
    String description;
    String textEvent;
    Instant startedAt;
    Instant finishedAt;
    String intent;                  //The intent of the request, used to determine the processing logic
}
