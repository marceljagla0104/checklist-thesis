package de.reutlingen_university.checklist.documentation;

import lombok.AccessLevel;
import lombok.Value;
import lombok.With;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.Set;

@Value
@With(AccessLevel.PRIVATE)
@Document("documentations")
public class Documentation {

    @MongoId
    String id;

    String operationId;

    String roomId;

    Set<String> entryIds;

    @Nullable
    String customText;

    Instant createdAt;

    @Nullable
    Instant savedAt;

    public static Documentation create(String operationId, String roomId) {
        String id = new ObjectId().toHexString();
        return new Documentation(id, operationId, roomId, Set.of(), null, Instant.now(), null);
    }

    public Documentation saveWithCustomText(String text) {
        return withCustomText(text).withSavedAt(Instant.now());
    }

}
