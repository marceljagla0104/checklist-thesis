package de.reutlingen_university.checklist.documentation;

import lombok.Value;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.List;

@Value
@Document("phrasings")
public class Phrase {

    @MongoId
    String id;

    List<String> descriptions; // alternative texts for text documentation

    Instant createdAt;

    public static Phrase create(List<String> texts) {
        String id = new ObjectId().toHexString();
        return new Phrase(id, texts, Instant.now());
    }
}
