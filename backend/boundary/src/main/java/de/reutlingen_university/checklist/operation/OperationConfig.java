package de.reutlingen_university.checklist.operation;

import de.reutlingen_university.checklist.ElementPhraseRepo;
import de.reutlingen_university.checklist.OperationHandler;
import de.reutlingen_university.checklist.documentation.PhraseRepo;
import de.reutlingen_university.checklist.operation.meta.MetaDataRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OperationConfig {

    @Bean
    OperationFacade operationFacade(
            OperationRepo operationRepo,
            SubprocessRepo subprocessRepo,
            MetaDataRepo metaDataRepo,
            PhraseRepo phraseRepo,
            ElementRepo elementRepo,
            ElementPhraseRepo elementPhraseRepo,
            ImageRepo imageRepo
    ) {
        OperationHandler handler = new OperationHandler(
                operationRepo,
                subprocessRepo,
                metaDataRepo,
                phraseRepo,
                imageRepo,
                elementRepo,
                elementPhraseRepo
        );
        return new OperationFacade(handler);
    }
}
