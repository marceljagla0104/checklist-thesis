package de.reutlingen_university.checklist.operation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.List;

@Value
@Document(collection = "elements")
@With()
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Element {

    @MongoId
    String id;

    List<Role> roles; // roles given by text annotation in the model

    Type type; // type of element. Reflects the BPMN type

    InputType inputType; // type of input element. Defines how the element is shown in the frontend

    String name; // name of the element. Most of the time displayed as label

    List<String> pathIds; // A stack of ids that represent the branches in the graph.

    List<String> eventIds; // Ids of boundary events that are connected to the element

    List<Child> children; // List of children elements that are connected to the element via a flow

    @Nullable
    String metaIdentifier; // Single use to match the element with a meta data... todo get rid of this

    @Nullable
    String metaDataId; // Id of the meta data that is connected to the element. Meta data is created after this element, so the metaIdentifier is used to match the element with the meta data first

    @Nullable
    String circulatingTriggerId; // id that represents a circulating path, that should be activated after an entry for this element is created

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static Element create(
            String id,
            List<Role> roles,
            Type type,
            InputType inputType,
            String name,
            List<String> pathIds,
            List<String> eventIds,
            List<Child> children,
            String metaIdentifier,
            String circulatingTriggerId
    ) {
        return new Element(
                id,
                roles,
                type,
                inputType,
                name,
                pathIds,
                eventIds,
                children,
                metaIdentifier,
                null,
                circulatingTriggerId,
                Instant.now(),
                null
        );
    }

    public Element markAsDeleted() {
        return this.withDeletedAt(Instant.now());
    }
}
