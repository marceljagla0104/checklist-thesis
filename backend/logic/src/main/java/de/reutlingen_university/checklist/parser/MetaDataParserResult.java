package de.reutlingen_university.checklist.parser;

import de.reutlingen_university.checklist.ElementMetaData;
import de.reutlingen_university.checklist.operation.meta.Image;
import de.reutlingen_university.checklist.operation.meta.MetaData;
import lombok.Value;
import lombok.With;

import java.util.List;

@Value
@With
public class MetaDataParserResult {

    List<ElementMetaData> relations;

    List<MetaData> metaData;

    List<Image> images;

    public static MetaDataParserResult create() {
        return new MetaDataParserResult(List.of(), List.of(), List.of());
    }

}
