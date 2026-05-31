package de.reutlingen_university.checklist.operation;

import lombok.Value;
import org.springframework.lang.Nullable;

import java.io.File;
import java.util.List;

@Value
public class CreateOperationCmd {

    File opModell;

    @Nullable
    File meta;

    @Nullable
    File alternativeTexts;

    List<File> additionalFiles;
}
