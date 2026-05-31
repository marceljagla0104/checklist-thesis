package de.reutlingen_university.checklist

import de.reutlingen_university.checklist.parser.BPMNParser
import de.reutlingen_university.checklist.parser.BPMNParserResult
import de.reutlingen_university.checklist.parser.MetaDataParser
import de.reutlingen_university.checklist.parser.MetaDataParserResult
import spock.lang.Specification

class ParserTest extends Specification {

    def "test parse"() {
        given: "a bpmn file"
        def xml = new File("src/test/resources/OpModell.bpmn")

        when: "the xml gets parsed"
        BPMNParserResult result = BPMNParser.parseBPMN(xml)

        then: "the operation is build correctly"
        result.operation != null
        result.operation.name == "Cochlear-Operation"
        result.subprocesses.size() == 5

        and: "the subprocesses have the right amount of elements"
        result.subprocesses.find({ it.name == "Nachbereitung" }).elementIds.size() == 6

        and: "all elements are build"
        result.elements.size() == result.subprocesses*.elementIds*.size().sum()
    }

    def "test parse with subprocess on third layer"() {
        given: "a bpmn with a subprocess within a subprocess"
        def xml = new File("src/test/resources/SubprocessTestFile.bpmn")

        when: "the bpmn is parsed"
        BPMNParserResult result = BPMNParser.parseBPMN(xml)

        then: "the operation is build correlty"
        result.operation != null
        result.operation.name == "Beispiel2"
        result.subprocesses.size() == 5

        and: "the subprocesses have the right amount of elements"
        result.subprocesses.find({ it.name == "Nachbereitung" }).elementIds.size() == 13

        and: "all elements are build"
        result.elements.size() == result.subprocesses*.elementIds*.size().sum()
    }

    def "test meta parser"() {
        given: "the process model"
        def xml = new File("src/test/resources/OpModell.bpmn")
        BPMNParserResult result = BPMNParser.parseBPMN(xml)

        and: "the additional files"
        def additional = List.of(new File("src/test/resources/posteriore_tympanotomie.temp.png"))

        when: "parsing the meta data"
        def meta = new File("src/test/resources/META.yaml")
        MetaDataParserResult metaResult = MetaDataParser.parse(
                result.getOperation().getId(),
                result.getElements(),
                meta,
                additional
        )

        then: "there should be three relations for 'Mikroblutungen stillen' with two instruments"
        def targetElements = result.getElements().stream().filter { it.getName() == "Mikroblutungen stillen" }.toList()
        targetElements.size() == 3

        def relation1 = metaResult.getRelations().stream().filter { it.getElementId() == targetElements[0].getId() }.toList()
        relation1.size() == 1

        def relation2 = metaResult.getRelations().stream().filter { it.getElementId() == targetElements[1].getId() }.toList()
        relation2.size() == 1

        def relation3 = metaResult.getRelations().stream().filter { it.getElementId() == targetElements[2].getId() }.toList()
        relation3.size() == 1
    }
}
