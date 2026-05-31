package de.reutlingen_university.checklist.parser;

import de.reutlingen_university.checklist.operation.Element;
import de.reutlingen_university.checklist.operation.Operation;
import de.reutlingen_university.checklist.operation.Subprocess;
import lombok.Value;

import java.util.List;

@Value
public class BPMNParserResult {

    Operation operation;
    List<Subprocess> subprocesses;
    List<Element> elements;


    public static BPMNParserResult of(Operation operation, List<Subprocess> subprocesses, List<Element> elements) {
        return new BPMNParserResult(operation, subprocesses, elements);
    }

}
