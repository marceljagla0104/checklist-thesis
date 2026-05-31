package de.reutlingen_university.checklist.documentation.mongodb;

import de.reutlingen_university.checklist.documentation.Documentation;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class DocumentationMongoRepoCustomImpl implements DocumentationMongoRepoCustom {

    ReactiveMongoTemplate template;

    @Override
    public Mono<Void> deleteElementIdFromDocumentation(String documentationId, String id) {
        Query query = new Query(Criteria.where("id").is(documentationId));
        Update update = new Update().pull("elementIds", id);

        return template.updateFirst(query, update, Documentation.class).then();
    }
}
