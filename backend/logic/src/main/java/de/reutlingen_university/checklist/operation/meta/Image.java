package de.reutlingen_university.checklist.operation.meta;

import lombok.Value;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Value
@Document(collection = "images")
public class Image {

    @MongoId
    String id;

    String url; // url to call to display image

    String caption;

    Instant createdAt;

    public static Image create(String url, String caption) {
        String id = new ObjectId().toHexString();
        return new Image(id, url, caption, Instant.now());
    }
}
