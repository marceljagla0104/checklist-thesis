package de.reutlingen_university.checklist.operation;

import de.reutlingen_university.checklist.OperationHandler;
import de.reutlingen_university.checklist.documentation.Phrase;
import de.reutlingen_university.checklist.operation.meta.Image;
import de.reutlingen_university.checklist.operation.meta.MetaData;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class OperationFacade {

    private final OperationHandler handler;

    public Flux<Subprocess> getSubprocesses(List<String> subprocessIds) {
        return handler.getSubprocesses(subprocessIds);
    }

    public Flux<Element> getElements(List<String> elementIds) {
        return handler.getElements(elementIds);
    }

    public Flux<MetaData> getMetaDataByElementIds(List<String> elementIds) {
        return handler.getMetaDataByElementIds(elementIds);
    }

    public Flux<Phrase> getPhrasesByElementIds(List<String> strings) {
        return handler.getPhrasesByElementIds(strings);
    }

    // Returns all operations as list item
    public Flux<OperationsListItemDTO> getOperations() {
        return handler.getOperations()
                .map(op -> new OperationsListItemDTO(op.getId(), op.getName(), op.getCreatedAt()));

    }

    // Returns a single operation by its id
    public Mono<OperationDTO> getOperation(String operationId) {
        return handler.getOperationView(operationId)
                .map(OperationTransformer::toDTO);
    }

    public Flux<Image> getImages(Set<String> imageIds) {
        return handler.getImages(imageIds);
    }

    // used to display the image in the frontend
    public Mono<byte[]> getImage(String operationId, String imageName) {
        return handler.getImage(operationId, imageName);
    }
}
