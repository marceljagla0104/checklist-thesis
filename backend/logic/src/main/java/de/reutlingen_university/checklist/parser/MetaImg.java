package de.reutlingen_university.checklist.parser;


import lombok.Value;

import java.util.Map;

@Value
public class MetaImg {

    String file;
    String caption;

    public static MetaImg fromMap(Map<String, String> map) {
        return new MetaImg(map.get("file"), map.get("caption"));
    }
}
