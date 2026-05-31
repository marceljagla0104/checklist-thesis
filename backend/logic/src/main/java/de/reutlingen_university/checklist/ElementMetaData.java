package de.reutlingen_university.checklist;


import lombok.Value;
import lombok.With;


//todo get rid of this
@Value
@With
public class ElementMetaData {

    String elementId;

    String metaDataId;


    public static ElementMetaData create(String elementId, String metaDataId) {
        return new ElementMetaData(elementId, metaDataId);
    }

}
