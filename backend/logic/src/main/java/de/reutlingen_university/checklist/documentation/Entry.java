package de.reutlingen_university.checklist.documentation;

import lombok.Value;
import lombok.With;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Value
@With
@Document("entries")
@CompoundIndex(name = "doc_element_idx", def = "{'documentationId' : 1, 'elementId': 1}", unique = true)
public class Entry {

    @MongoId
    String id;

    String documentationId;

    //todo documentation or room is redundant when session is tracked
    String roomId;

    String elementId; // operation element that entry is created for

    String description; // description text that shows up in text documentation

    @Nullable
    String textEvent;  // field to track an unforeseen event by text

    @Nullable
    Instant startedAt;

    @Nullable
    Instant finishedAt;

    Instant createdAt;

    boolean ignore; // flag to ignore entry in text documentation

    public static Entry create(
            String elementId,
            String documentationId,
            String room,
            String description,
            Instant startedAt,
            Instant finishedAt
    ) {
        String id = new ObjectId().toHexString();
        return new Entry(
                id,
                documentationId,
                room,
                elementId,
                description,
                null,
                startedAt,
                finishedAt,
                Instant.now(),
                false
        );
    }

    public static Entry createIgnored(
            String elementId,
            String documentationId,
            String room,
            String description,
            Instant startedAt,
            Instant finishedAt
    ) {
        String id = new ObjectId().toHexString();
        return new Entry(
                id,
                documentationId,
                room,
                elementId,
                description,
                null,
                startedAt,
                finishedAt,
                Instant.now(),
                true
        );
    }

    public Entry update(String description, String textEvent, Instant startedAt, Instant finishedAt) {
        Entry updated = this;
        updated = updated.withDescription(description);
        updated = Optional.ofNullable(textEvent).map(updated::withTextEvent).orElse(updated);
        updated = Optional.ofNullable(startedAt).map(updated::withStartedAt).orElse(updated);
        updated = updated.withFinishedAt(finishedAt);

        return updated;
    }

    public Optional<Instant> getStartedAt() {
        return Optional.ofNullable(startedAt);
    }

    public Optional<Instant> getFinishedAt() {
        return Optional.ofNullable(finishedAt);
    }

    public Long getDuration(List<Entry> entries) {
        if (finishedAt != null) {
            if (startedAt != null) {
                // return duration between start and end
                return finishedAt.getEpochSecond() - startedAt.getEpochSecond();
            }

            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).getId().equals(id)) {
                    if (i + 1 < entries.size()) {
                        Entry nextEntry = entries.get(i + 1);

                        // return duration between finishing of this entry and creation of next entry
                        // taken from prototype version of checklist, maybe not best solution
                        return nextEntry.getCreatedAt().getEpochSecond() - finishedAt.getEpochSecond();
                    }
                }
            }
        }

        return null;
    }

    public Long getCalculatedDuration(List<Entry> entries) {
        Long duration = this.getDuration(entries);

        // find all entries that started and finished between this entry's start and finish
        // and sum their durations
        // and subtract them from duration
        if (duration != null) {
            for (Entry entry : entries) {
                if (entry.getStartedAt().isPresent() && entry.getFinishedAt().isPresent()) {
                    if (this.getStartedAt().isPresent() && this.getFinishedAt().isPresent()) {
                        if (entry.getStartedAt().isPresent() && entry.getFinishedAt().isPresent()) {
                            boolean entryStartedAndFinishedDuringCurrent =
                                    this.getStartedAt().get().isBefore(entry.getStartedAt().get()) &&
                                    this.getFinishedAt().get().isAfter(entry.getFinishedAt().get());

                            if (entryStartedAndFinishedDuringCurrent) {
                                duration -= entry.getDuration(entries);
                            }
                        }
                    }

                }
            }
        }
        return duration;
    }
}
