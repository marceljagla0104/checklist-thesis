package de.reutlingen_university.checklist.documentation;

import de.reutlingen_university.checklist.ElementPhraseRepo;
import de.reutlingen_university.checklist.OperationHandler;
import de.reutlingen_university.checklist.operation.ElementRepo;
import de.reutlingen_university.checklist.operation.ImageRepo;
import de.reutlingen_university.checklist.operation.OperationRepo;
import de.reutlingen_university.checklist.operation.SubprocessRepo;
import de.reutlingen_university.checklist.operation.meta.MetaDataRepo;
import de.reutlingen_university.checklist.sync.websockets.SessionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentationConfig {

    @Bean
    public DocumentationFacade documentationFacade(
            EntryRepo entryRepo,
            DocumentationRepo documentationRepo,
            SessionService sessionService,
            ElementPhraseRepo elementPhraseRepo,
            PhraseRepo phraseRepo,
            OperationRepo operationRepo,
            ElementRepo elementRepo,
            SubprocessRepo subprocessRepo,
            MetaDataRepo metaDataRepo,
            ImageRepo imageRepo
    ) {
        DocumentationHandler handler = new DocumentationHandler(
                entryRepo,
                documentationRepo,
                elementPhraseRepo,
                phraseRepo
        );

        OperationHandler operationHandler = new OperationHandler(
                operationRepo,
                subprocessRepo,
                metaDataRepo,
                phraseRepo,
                imageRepo,
                elementRepo,
                elementPhraseRepo
        );

        return new DocumentationFacade(handler, sessionService, operationHandler);
    }
}
