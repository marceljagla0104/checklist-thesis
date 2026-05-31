package de.reutlingen_university.checklist.admin;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminConfig {

    @Bean
    public AdminFacade adminFacade(
            EntryRepo entryRepo,
            DocumentationRepo documentationRepo,
            ElementPhraseRepo elementPhraseRepo,
            PhraseRepo phraseRepo,
            OperationRepo operationRepo,
            SubprocessRepo subprocessRepo,
            MetaDataRepo metaDataRepo,
            ImageRepo imageRepo,
            ElementRepo elementRepo
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
        return new AdminFacade(handler, operationHandler);
    }
}
