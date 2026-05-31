package de.reutlingen_university.checklist.parser;

import de.reutlingen_university.checklist.ElementPhrase;
import de.reutlingen_university.checklist.documentation.Phrase;
import de.reutlingen_university.checklist.operation.Element;
import lombok.AllArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class PhraseParser {

    public static PhraseParserResult parse(List<Element> elements, File alternativeTexts) {
        if (alternativeTexts == null) {
            return PhraseParserResult.create();
        }
        try (FileInputStream fis = new FileInputStream(alternativeTexts)) {
            Yaml yaml = new Yaml();
            Map<String, List<String>> yamlFile = yaml.load(fis);

            PhraseParserResult result = PhraseParserResult.create();
            List<Phrase> phrases = new ArrayList<>();
            List<ElementPhrase> elementPhrases = new ArrayList<>();

            List<String> alreadySeenNames = new ArrayList<>();

            for (Element element : elements) {
                String name = element.getName();
                if (alreadySeenNames.contains(name)) {
                    continue;
                }

                alreadySeenNames.add(name);
                List<String> descriptions = yamlFile.get(name);

                if (descriptions != null) {
                    Phrase phrase = Phrase.create(descriptions);
                    phrases.add(phrase);
                    elementPhrases.add(ElementPhrase.create(element.getId(), phrase.getId()));
                }
            }

            return result.withPhrases(phrases).withRelations(elementPhrases);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse YAML file", e);
        }
    }
}
