package de.reutlingen_university.checklist.admin;

import de.reutlingen_university.checklist.OperationHandler;
import de.reutlingen_university.checklist.documentation.DocumentationHandler;
import de.reutlingen_university.checklist.mongo.*;
import de.reutlingen_university.checklist.operation.OperationFacade;

public class AdminFacadeTestConfig {

    private static final OperationTestRepo operationRepo = new OperationTestRepo();
    private static final SubprocessTestRepo subprocessRepo = new SubprocessTestRepo();
    private static final MetaDataTestRepo metaDataRepo = new MetaDataTestRepo();
    private static final PhraseTestRepo phraseRepo = new PhraseTestRepo();
    private static final ImageTestRepo imageRepo = new ImageTestRepo();
    private static final ElementTestRepo elementRepo = new ElementTestRepo();
    private static final ElementPhraseTestRepo elementPhraseRepo = new ElementPhraseTestRepo();

    AdminFacade adminFacade() {

        OperationHandler operationHandler = new OperationHandler(
                operationRepo,
                subprocessRepo,
                metaDataRepo,
                phraseRepo,
                imageRepo,
                elementRepo,
                elementPhraseRepo
        );

        DocumentationHandler documentationHandler = new DocumentationHandler(
                new EntryTestRepo(),
                new DocumentationTestRepo(),
                new ElementPhraseTestRepo(),
                new PhraseTestRepo()
        );

        return new AdminFacade(documentationHandler, operationHandler);
    }

    OperationFacade operationFacade() {

        OperationHandler operationHandler = new OperationHandler(
               operationRepo,
                subprocessRepo,
                metaDataRepo,
                phraseRepo,
                imageRepo,
                elementRepo,
                elementPhraseRepo
        );

        return new OperationFacade(operationHandler);
    }

}
