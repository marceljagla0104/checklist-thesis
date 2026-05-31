package de.reutlingen_university.checklist;

import de.reutlingen_university.checklist.documentation.Phrase;
import de.reutlingen_university.checklist.documentation.PhraseRepo;
import de.reutlingen_university.checklist.operation.*;
import de.reutlingen_university.checklist.operation.meta.*;
import de.reutlingen_university.checklist.parser.*;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
public class OperationHandler {

    private final OperationRepo operationRepo;
    private final SubprocessRepo subprocessRepo;

    private final MetaDataRepo metaDataRepo;
    private final PhraseRepo phraseRepo;
    private final ImageRepo imageRepo;

    private final ElementRepo elementRepo;
    private final ElementPhraseRepo elementPhraseRepo;

    // parses all information from the given operation model and saves it to the database
    public Mono<String> createOperation(CreateOperationCmd cmd) {
        BPMNParserResult bpmnResult = BPMNParser.parseBPMN(cmd.getOpModell());

        MetaDataParserResult metaResult = MetaDataParser.parse(
                bpmnResult.getOperation().getId(),
                bpmnResult.getElements(),
                cmd.getMeta(),
                cmd.getAdditionalFiles()
        );

        List<Element> elements = bpmnResult.getElements().stream().map(e -> {
                    String metaDataId = metaResult.getRelations().stream()
                            .filter(emd -> emd.getElementId().equals(e.getId()))
                            .map(ElementMetaData::getMetaDataId)
                            .findFirst()
                            .orElse(null);

                    return e.withMetaDataId(metaDataId);
                })
                .toList();

        PhraseParserResult phraseResult = PhraseParser.parse(bpmnResult.getElements(), cmd.getAlternativeTexts());
        return operationRepo.save(bpmnResult.getOperation())
                .thenMany(subprocessRepo.saveAll(bpmnResult.getSubprocesses()))
                .thenMany(elementRepo.saveAll(elements))
                .thenMany(metaDataRepo.saveAll(metaResult.getMetaData()))
                .thenMany(phraseRepo.saveAll(phraseResult.getPhrases()))
                .thenMany(elementPhraseRepo.saveAll(phraseResult.getRelations()))
                .thenMany(imageRepo.saveAll(metaResult.getImages()))
                .then()
                .thenReturn(bpmnResult.getOperation().getId());
    }

    public Flux<Subprocess> getSubprocesses(List<String> ids) {
        return subprocessRepo.findAllById(ids);
    }

    public Flux<Element> getElements(List<String> elementIds) {
        return elementRepo.findAllById(elementIds);
    }

    public Flux<Element> getElementsByPathId(String pathId) {
        return elementRepo.findAllByPathId(pathId);
    }

    public Flux<MetaData> getMetaDataByElementIds(List<String> elementIds) {

        return elementRepo.findAllById(elementIds)
                .mapNotNull(Element::getMetaDataId)
                .collectList()
                .flatMapMany(metaDataRepo::findAllByIds);
    }

    public Flux<Phrase> getPhrasesByElementIds(List<String> elementIds) {
        return elementPhraseRepo.findByElementIdIn(elementIds)
                .map(ElementPhrase::getPhraseId)
                .collectList()
                .flatMapMany(phraseRepo::findAllByIds);
    }

    public Flux<Operation> getOperations() {
        return operationRepo.findAll().filter(o -> o.getDeletedAt().isEmpty());
    }

    public Mono<OperationView> getOperationView(String id) {
        return operationRepo.findById(id)
                .flatMap(operation -> this.getSubprocessViews(operation.getSubprocessIds())
                        .collectList()
                        .map(subprocesses -> new OperationView(
                                operation.getId(),
                                operation.getName(),
                                subprocesses,
                                operation.getCreatedAt()
                        )));
    }


    private Flux<SubprocessView> getSubprocessViews(List<String> ids) {
        return subprocessRepo.findAllById(ids)
                .concatMap(subprocess -> this.getElementViews(subprocess.getElementIds())
                        .collectList()
                        .map(elements -> new SubprocessView(
                                subprocess.getId(),
                                subprocess.getName(),
                                elements,
                                subprocess.getCreatedAt()
                        )));
    }

    private Flux<ElementView> getElementViews(List<String> elementIds) {
        Flux<Element> elements$ = this.getElements(elementIds);
        return elements$.concatMap(element -> {
                    String metaDataId = Optional.ofNullable(element.getMetaDataId()).orElse("");
                    return metaDataRepo.findById(metaDataId)
                            .flatMap(metaData -> buildElementViewWithMetaData(element, metaData))
                            .switchIfEmpty(buildElementViewWithoutMetaData(element));
                }).collectMap(ElementView::getId)
                .flatMapMany(map -> Flux.fromIterable(elementIds) // this sorts the elements in the same order as the elementIds
                        .map(map::get)); // asynchronous mapping with flatMap would not guarantee the order
    }

    public Flux<Image> getImages(Set<String> imageIds) {
        return imageRepo.findAllById(imageIds);
    }

    public Mono<byte[]> getImage(String operationId, String imageName) {
        return Mono.fromCallable(() -> {
            String env = System.getProperty("env", "main");
            Path imagePath = Paths.get(
                    "src",
                    env,
                    "resources",
                    "static",
                    "data",
                    operationId,
                    imageName
            );

            if (Files.exists(imagePath)) {
                return Files.readAllBytes(imagePath);
            } else {
                return null; // todo Handle missing images
            }
        });
    }

    private static Mono<ElementView> buildElementViewWithoutMetaData(Element element) {
        return Mono.just(ElementView.withoutMetaData(
                element.getId(),
                element.getRoles(),
                element.getType(),
                element.getInputType(),
                element.getName(),
                element.getPathIds(),
                element.getEventIds(),
                element.getChildren(),
                element.getCirculatingTriggerId(),
                element.getCreatedAt()
        ));
    }


    private Mono<ElementView> buildElementViewWithMetaData(Element element, MetaData metaData) {
        SurgeonData surgeonData = metaData.getSurgeonData();
        StudentData studentData = metaData.getStudentData();
        ScrubData scrubData = metaData.getScrubData();

        Set<String> surgeonImageIds = Optional.ofNullable(surgeonData)
                .map(s -> new HashSet<>(s.getImageIds()))
                .orElse(new HashSet<>());

        Set<String> studentImageIds = Optional.ofNullable(studentData)
                .map(s -> new HashSet<>(s.getImageIds()))
                .orElse(new HashSet<>());

        return Mono.zip(
                        imageRepo.findAllById(surgeonImageIds).collectList(),
                        imageRepo.findAllById(studentImageIds).collectList()
                )
                .map(it -> new ElementView(
                        element.getId(),
                        element.getRoles(),
                        element.getType(),
                        element.getInputType(),
                        element.getName(),
                        element.getPathIds(),
                        element.getEventIds(),
                        element.getChildren(),
                        surgeonData != null
                                ? surgeonData.getInfo()
                                : null,
                        it.getT1(),
                        studentData != null
                                ? studentData.getDescription()
                                : null,
                        it.getT2(),
                        scrubData != null
                                ? scrubData.getInstruments()
                                : null,
                        element.getCirculatingTriggerId(),
                        element.getCreatedAt()
                ));
    }

    public Mono<Void> deleteOperation(String operationId) {
        // delete everything but phrasings and meta data. Could be reused in the future.
        return operationRepo.findById(operationId)
                .flatMapMany(operation -> subprocessRepo.findAllById(operation.getSubprocessIds()))
                .flatMap(subprocess -> {
                    Mono<Void> deleteElements$ = elementRepo.findAllById(subprocess.getElementIds())
                            .map(Element::markAsDeleted)
                            .collectList()
                            .flatMapMany(elementRepo::saveAll)
                            .then();


                    Mono<Void> deleteElementPhrases$ = elementPhraseRepo.findByElementIdIn(subprocess.getElementIds())
                            .map(ElementPhrase::markAsDeleted)
                            .collectList()
                            .flatMapMany(elementPhraseRepo::saveAll)
                            .then();

                    Mono<Void> deleteSubprocess$ = subprocessRepo.findById(subprocess.getId())
                            .map(Subprocess::markAsDeleted)
                            .flatMapMany(subprocessRepo::save)
                            .then();

                    return deleteElements$
                            .then(deleteElementPhrases$)
                            .then(deleteSubprocess$);
                })
                .then(operationRepo.findById(operationId)
                        .map(Operation::markAsDeleted)
                        .flatMap(operationRepo::save)
                        .then());

    }
}
