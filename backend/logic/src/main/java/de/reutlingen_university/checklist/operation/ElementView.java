package de.reutlingen_university.checklist.operation;

import de.reutlingen_university.checklist.operation.meta.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Getter
public class ElementView { // merges meta data and element data

    String id;

    List<Role> roles;

    Type type;

    InputType inputType;

    String name;

    List<String> pathIds;

    List<String> eventIds;

    List<Child> children;

    String surgeonInfo;

    List<Image> surgeonImages;

    String studentInfo;

    List<Image> studentImages;

    List<String> instruments;

    String circulatingTriggerId;

    Instant createdAt;

    public static ElementView withoutMetaData(
            String id,
            List<Role> roles,
            Type type,
            InputType inputType,
            String name,
            List<String> pathIds,
            List<String> eventIds,
            List<Child> children,
            String circulatingTriggerId,
            Instant createdAt
    ) {
        return new ElementView(
                id,
                roles,
                type,
                inputType,
                name,
                pathIds,
                eventIds,
                children,
                null,
                null,
                null,
                null,
                null,
                circulatingTriggerId,
                createdAt
        );
    }
}
