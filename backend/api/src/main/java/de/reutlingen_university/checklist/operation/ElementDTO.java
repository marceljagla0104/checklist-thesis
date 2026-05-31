package de.reutlingen_university.checklist.operation;

import de.reutlingen_university.checklist.ImageDTO;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
public class ElementDTO {
    String id;

    List<String> roles;

    String type;

    String inputType;

    String name;

    List<String> pathIds;

    List<String> eventIds;

    List<ChildDTO> children;

    List<String> instruments;

    String studentInfo;

    List<ImageDTO> studentImages;

    String surgeonInfo;

    List<ImageDTO> surgeonImages;

    String circulatingTriggerId;

    List<String> allChildIds;

    Instant createdAt;

}
