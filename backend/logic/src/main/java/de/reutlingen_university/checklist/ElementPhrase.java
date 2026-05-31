package de.reutlingen_university.checklist;


import lombok.Value;
import lombok.With;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Value
@With
@Document(collection = "element_phrases")
public class ElementPhrase {
    @MongoId
    String id;

    String elementId;

    String phraseId;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static ElementPhrase create(String elementId, String phraseId) {
        String id = new ObjectId().toHexString();
        return new ElementPhrase(id, elementId, phraseId, Instant.now(), null);
    }

    public ElementPhrase markAsDeleted() {
        return this.withDeletedAt(Instant.now());
    }
}
