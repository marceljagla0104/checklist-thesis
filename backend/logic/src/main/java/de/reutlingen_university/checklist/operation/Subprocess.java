package de.reutlingen_university.checklist.operation;

import lombok.Value;
import lombok.With;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.List;

@Value
@Document(collection = "subprocesses")
@With
public class Subprocess {

    @MongoId
    String id;

    String name; // phase name to display

    List<String> elementIds;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static Subprocess create(String name, List<String> elementIds) {
        String id = new ObjectId().toHexString();
        return new Subprocess(id, name, elementIds, Instant.now(), null);
    }

    public Subprocess markAsDeleted() {
        return withDeletedAt(Instant.now());
    }
}
