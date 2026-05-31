package de.reutlingen_university.checklist.sync;

import de.reutlingen_university.checklist.ElementPhraseRepo;
import de.reutlingen_university.checklist.OperationHandler;
import de.reutlingen_university.checklist.documentation.DocumentationHandler;
import de.reutlingen_university.checklist.documentation.DocumentationRepo;
import de.reutlingen_university.checklist.documentation.EntryRepo;
import de.reutlingen_university.checklist.documentation.PhraseRepo;
import de.reutlingen_university.checklist.operation.ElementRepo;
import de.reutlingen_university.checklist.operation.ImageRepo;
import de.reutlingen_university.checklist.operation.OperationRepo;
import de.reutlingen_university.checklist.operation.SubprocessRepo;
import de.reutlingen_university.checklist.operation.meta.MetaDataRepo;
import de.reutlingen_university.checklist.sync.websockets.SessionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SyncConfig {
    @Bean
    SyncFacade syncFacade(
            SessionService sessionService,
            OperationRepo operationRepo,
            SubprocessRepo subprocessRepo,
            MetaDataRepo metaDataRepo,
            PhraseRepo phraseRepo,
            ElementRepo elementRepo,
            ElementPhraseRepo elementPhraseRepo,
            ImageRepo imageRepo,
            EntryRepo entryRepo,
            DocumentationRepo documentationRepo
    ) {
        OperationHandler checklistHandler = new OperationHandler(
                operationRepo,
                subprocessRepo,
                metaDataRepo,
                phraseRepo,
                imageRepo,
                elementRepo,
                elementPhraseRepo
        );

        DocumentationHandler documentationHandler = new DocumentationHandler(
                entryRepo,
                documentationRepo,
                elementPhraseRepo,
                phraseRepo
        );
        return new SyncFacade(sessionService, checklistHandler, documentationHandler);
    }
}
