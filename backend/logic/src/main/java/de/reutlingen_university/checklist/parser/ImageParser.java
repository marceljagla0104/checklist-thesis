package de.reutlingen_university.checklist.parser;

import de.reutlingen_university.checklist.operation.meta.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ImageParser {

    public static Image parse(List<File> files, String operationId, String fileName, String caption) {
        return files.stream()
                    .filter(file -> removeTempSuffix(file.getName()).equals(fileName))
                    .findFirst()
                    .map(file -> createFileAndImage(file, operationId, caption))
                    .orElseThrow(() -> new RuntimeException("File not found"));
    }

    // writes the image file into the static resources folder
    private static Image createFileAndImage(File file, String operationId, String caption) {
        String env = System.getProperty("env", "main");
        try {
            Path sourcePath = file.toPath();
            Path targetPath = Paths.get(
                    "src",
                    env,
                    "resources",
                    "static",
                    "data",
                    operationId,
                    removeTempSuffix(file.getName())
            );

            if (!Files.exists(targetPath.getParent())) {
                Files.createDirectories(targetPath.getParent());
            }

            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            return Image.create(
                    "/" + operationId + "/" + removeTempSuffix(file.getName()),
                    caption
            );
        } catch (IOException e) {
            throw new RuntimeException("Error copying file", e);
        }
    }

    private static String removeTempSuffix(String name) {
        List<String> parts = List.of(name.split("\\."));
        return parts.get(0) + "." + parts.get(2);
    }
}
