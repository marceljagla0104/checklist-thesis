package de.reutlingen_university.checklist.operation;

import lombok.Value;
import lombok.With;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Value
@With
@Document("operations")
public class Operation {

    @MongoId
    String id;

    String name;

    List<String> subprocessIds;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static Operation create(String name, List<Subprocess> subprocesses) {
        String id = new ObjectId().toHexString();
        List<String> subIds = subprocesses.stream().map(Subprocess::getId).toList();

        return new Operation(id, name, subIds, Instant.now(), null);
    }

    public Operation markAsDeleted() {
        return this.withDeletedAt(Instant.now());
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }
}
