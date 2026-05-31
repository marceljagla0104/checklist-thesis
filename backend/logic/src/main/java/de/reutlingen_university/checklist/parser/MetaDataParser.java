package de.reutlingen_university.checklist.parser;

import de.reutlingen_university.checklist.ElementMetaData;
import de.reutlingen_university.checklist.operation.Element;
import de.reutlingen_university.checklist.operation.Role;
import de.reutlingen_university.checklist.operation.meta.*;
import lombok.AllArgsConstructor;
import org.yaml.snakeyaml.Yaml;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

@AllArgsConstructor
public class MetaDataParser {


    public static MetaDataParserResult parse(
            String operationId,
            List<Element> elements,
            File file,
            List<File> additionalFiles
    ) {
        if (file == null) {
            return MetaDataParserResult.create();
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlFile = yaml.load(fis);

            MetaDataParserResult result = MetaDataParserResult.create();
            Map<String, MetaData> metaDataList = new HashMap<>();
            List<ElementMetaData> elementMetaDataList = new ArrayList<>();

            Set<String> seenIdentifiers = new HashSet<>();
            Set<Image> images = new HashSet<>();
            for (Element element : elements) {
                if (element.getMetaIdentifier() != null) {
                    if (!seenIdentifiers.contains(element.getMetaIdentifier())) {
                        //build new meta data, parse images and connect to element
                        seenIdentifiers.add(element.getMetaIdentifier());
                        Tuple2<MetaData, List<Image>> tuple = parseMetaDataAndImages(
                                yamlFile,
                                element.getMetaIdentifier(),
                                additionalFiles,
                                operationId,
                                images
                        );

                        MetaData metaData = tuple.getT1();
                        List<Image> imageList = tuple.getT2();

                        // connect meta data to element
                        ElementMetaData elementMetaData = ElementMetaData.create(
                                element.getId(),
                                metaData.getId()
                        );

                        metaDataList.put(element.getMetaIdentifier(), metaData);
                        elementMetaDataList.add(elementMetaData);
                        images.addAll(imageList);
                    } else {
                        // connect existing meta data to element
                        MetaData metaData = metaDataList.get(element.getMetaIdentifier());

                        ElementMetaData elementMetaData = ElementMetaData.create(
                                element.getId(),
                                metaData.getId()
                        );
                        elementMetaDataList.add(elementMetaData);
                    }
                }
            }

            result = result.withMetaData(metaDataList.values().stream().toList());
            result = result.withRelations(elementMetaDataList);
            result = result.withImages(images.stream().toList());
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse YAML file", e);
        }
    }

    private static Tuple2<MetaData, List<Image>> parseMetaDataAndImages(
            Map<String, Object> yamlFile,
            String identifier,
            List<File> additonalFiles,
            String operationId,
            Set<Image> alreadySavedImages
    ) {
        Map<String, Object> meta = (Map<String, Object>) yamlFile.get(identifier);

        if (meta == null) {
            System.out.println("Meta Data for" + identifier + "is null");
            //todo throw exception
        }
        Map<String, Object> surgeon = (Map<String, Object>) meta.get(Role.SURGEON.toString());
        Map<String, Object> student = (Map<String, Object>) meta.get(Role.STUDENT.toString());
        Map<String, Object> scrub = (Map<String, Object>) meta.get(Role.SCRUB.toString());

        MetaData metaData = MetaData.create();
        List<Image> images = new ArrayList<>();

        if (surgeon != null) {
            String info = (String) surgeon.get("info");
            List<MetaImg> metaImages = getMetaImages(surgeon);

            List<Image> tempImages = buildAndSaveImages(additonalFiles, operationId, metaImages, alreadySavedImages);
            List<String> imageIds = tempImages.stream().map(Image::getId).toList();
            SurgeonData surgeonData = new SurgeonData(info, imageIds);

            images.addAll(tempImages);
            metaData = metaData.withSurgeonData(surgeonData);
        }

        if (student != null) {
            String description = (String) student.get("description");
            List<MetaImg> metaImages = getMetaImages(student);
            Set<Image> combined = new HashSet<>();
            combined.addAll(alreadySavedImages);
            combined.addAll(images);

            List<Image> tempImages = buildAndSaveImages(additonalFiles, operationId, metaImages, combined);
            List<String> imageIds = tempImages.stream().map(Image::getId).toList();
            StudentData studentData = new StudentData(description, imageIds);
            images.addAll(tempImages);

            metaData = metaData.withStudentData(studentData);
        }

        if (scrub != null) {
            List<String> instruments = (List<String>) scrub.get("instruments");

            ScrubData scrubData = new ScrubData(instruments);
            metaData = metaData.withScrubData(scrubData);
        }


        return Tuples.of(metaData, images);
    }

    private static List<Image> buildAndSaveImages(
            List<File> additionalFiles,
            String operationId,
            List<MetaImg> metaImages,
            Set<Image> alreadySavedImages
    ) {
        return metaImages.stream()
                .map(metaImage -> ImageParser.parse(
                        additionalFiles,
                        operationId,
                        metaImage.getFile(),
                        metaImage.getCaption()
                ))
                .map(image -> {
                    boolean imageExists = alreadySavedImages.stream()
                            .anyMatch(i -> i.getUrl().equals(image.getUrl()));
                    if (!imageExists) {
                        return image;
                    }
                    return alreadySavedImages.stream()
                            .filter(i -> i.getUrl().equals(image.getUrl()))
                            .findFirst()
                            .orElseThrow();
                })
                .toList();
    }

    private static List<MetaImg> getMetaImages(Map<String, Object> roleMap) {
        List<Map<String, String>> imageMap = (List<Map<String, String>>) roleMap.get("images");
        if (imageMap == null) {
            return List.of();
        }

        return imageMap.stream()
                .map(MetaImg::fromMap)
                .toList();
    }
}


