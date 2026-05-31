package de.reutlingen_university.checklist.admin;

import de.reutlingen_university.checklist.OperationHandler;
import de.reutlingen_university.checklist.documentation.DocumentationHandler;
import de.reutlingen_university.checklist.operation.CreateOperationCmd;
import lombok.AllArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@AllArgsConstructor
public class AdminFacade {

    private final DocumentationHandler handler;
    private final OperationHandler operationHandler;

    private final String TEMP_FILE = "temp.zip";

    // Returns a List of all documentations for the admin view
    public Flux<DocumentationAdminDTO> listDocumentations() {
        return handler.listDocumentations()
                .flatMap(doc -> operationHandler.getOperationView(doc.getOperationId())
                        .map(operationView -> new DocumentationAdminDTO(
                                doc.getId(),
                                doc.getOperationId(),
                                operationView.getName(),
                                doc.getRoomId(),
                                doc.getCreatedAt(),
                                doc.getSavedAt()
                        )));
    }

    // Creates a new operation from a zip file
    // returns: the operation id
    @Transactional
    public Mono<String> createOperation(FilePart filePart) {
        return filePart.transferTo(Paths.get(TEMP_FILE))// zip is saved temporarily
                .then(Mono.fromCallable(this::zipToCmd)
                        .flatMap(operationHandler::createOperation))
                .publishOn(Schedulers.boundedElastic())
                .doFinally(signalType -> {
                    try {
                        Files.deleteIfExists(Paths.get(TEMP_FILE));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .onErrorResume(e -> Mono.error(new RuntimeException(
                        "An error occurred while creating the operation",
                        e
                )));
    }

    // helper method to unpack the zip
    private CreateOperationCmd zipToCmd() throws IOException {
        File alternativeTexts = null;
        File meta = null;
        File opModell = null;
        List<File> additionalFiles = new ArrayList<>();

        // saved zip is read and files are extracted
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(TEMP_FILE))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                switch (zipEntry.getName()) {
                    case "ALT.yaml" -> {
                        alternativeTexts = getFile(zis, zipEntry.getName());
                    }
                    case "META.yaml" -> meta = getFile(zis, zipEntry.getName());
                    case "OpModell.bpmn" -> opModell = getFile(zis, zipEntry.getName());
                    default -> {
                        File temp = getFile(zis, zipEntry.getName());
                        additionalFiles.add(temp);
                    }
                }
                zis.closeEntry();
            }
        }

        return new CreateOperationCmd(opModell, meta, alternativeTexts, additionalFiles);
    }

    // helper method to extract files from zip
    private static File getFile(ZipInputStream zis, String fileName) throws IOException {
        String prefix = fileName.substring(0, fileName.lastIndexOf("."));
        String suffix = fileName.substring(fileName.lastIndexOf("."));

        File tempFile = File.createTempFile(prefix + ".", suffix);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            return tempFile;
        }
    }

    // Marks an operation as deleted. Operation is not deleted from the database
    public Mono<Void> deleteOperation(String operationId) {
        return operationHandler.deleteOperation(operationId);
    }
}
