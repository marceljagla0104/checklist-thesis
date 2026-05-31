package de.reutlingen_university.checklist.operation.meta;

import lombok.Value;

import java.util.List;

@Value
public class StudentData {

    String description;

    List<String> imageIds;
}
