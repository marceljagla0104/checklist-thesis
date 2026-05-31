package de.reutlingen_university.checklist.operation;

import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
public class OperationDTO {

    String id;
    String name;
    List<SubprocessDTO> subprocesses;
    Instant createdAt;
}
