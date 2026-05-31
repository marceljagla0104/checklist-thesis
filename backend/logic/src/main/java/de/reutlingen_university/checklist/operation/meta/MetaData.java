package de.reutlingen_university.checklist.operation.meta;

import lombok.Value;
import lombok.With;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Value
@With()
@Document("meta_data")
public class MetaData {

    @MongoId
    String id;

    ScrubData scrubData;

    SurgeonData surgeonData;

    StudentData studentData;

    Instant createdAt;

    public static MetaData create() {
        String id = new ObjectId().toHexString();
        return new MetaData(id, null, null, null, Instant.now());
    }
}
