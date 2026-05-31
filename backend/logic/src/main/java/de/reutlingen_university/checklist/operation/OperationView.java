package de.reutlingen_university.checklist.operation;

import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
public class OperationView { // merges operation with subprocesses

    String id;
    String name;
    List<SubprocessView> subprocesses;
    Instant createdAt;

}
