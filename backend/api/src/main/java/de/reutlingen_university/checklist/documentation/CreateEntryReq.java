package de.reutlingen_university.checklist.documentation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEntryReq {

    String elementId;
    String description;
    String textEvent;
    Instant startedAt;
    Instant finishedAt;
    String intent;              //
    String role;                //
    String roomId;              //
}
