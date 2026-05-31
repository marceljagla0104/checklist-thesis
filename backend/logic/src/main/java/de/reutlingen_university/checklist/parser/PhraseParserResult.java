package de.reutlingen_university.checklist.parser;

import de.reutlingen_university.checklist.ElementPhrase;
import de.reutlingen_university.checklist.documentation.Phrase;
import lombok.Value;
import lombok.With;

import java.util.List;

@Value
@With
public class PhraseParserResult {

    List<Phrase> phrases;
    List<ElementPhrase> relations;

    public static PhraseParserResult create() {
        return new PhraseParserResult(List.of(), List.of());
    }
}
