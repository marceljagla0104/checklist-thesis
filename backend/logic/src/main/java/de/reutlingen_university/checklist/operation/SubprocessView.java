package de.reutlingen_university.checklist.operation;

import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
public class SubprocessView { // merges subprocess with elements

    String id;

    String name;

    List<ElementView> elements;

    Instant createdAt;

}
