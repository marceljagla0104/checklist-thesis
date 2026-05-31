package de.reutlingen_university.checklist.admin

import de.reutlingen_university.checklist.operation.ElementDTO
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.mock.web.MockMultipartFile
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.awt.image.DataBuffer
import java.nio.file.Path

class AdminFacade_createOperation_Test extends Specification {
    def facade = new AdminFacadeTestConfig().adminFacade()
    def operationFacade = new AdminFacadeTestConfig().operationFacade()

    def "should get all operations"() {
        given: "three new operations are created"
        def filePart = loadZipFile()
        facade.createOperation(filePart).block()
        facade.createOperation(filePart).block()
        facade.createOperation(filePart).block()

        when: "the operations are retrieved"
        def operations = operationFacade.getOperations().collectList().block()

        then: "there should be three operations"
        operations.size() == 3
        operations*.id != null
        operations*.name != null
        operations*.createdAt != null
    }

    def "should create operation and connect elements with meta data"(){
        given: "a zip file as FilePart"
        FilePart filePart = loadZipFile()

        when: "create operation is called"
        def operationId = facade.createOperation(filePart).block()
        def operation = operationFacade.getOperation(operationId).block()

        then: "operation is created"
        operation != null

        and: "the elements are connected with the meta data"
        List<ElementDTO> elements = operation.subprocesses*.elements.flatten()
        def elementsWithInstruments = elements.findAll { !(it.instruments == null) }
        !elementsWithInstruments.isEmpty()

        def elementsWithStudentInformation = elements.findAll { !(it.studentInfo == null) }
        !elementsWithStudentInformation.isEmpty()
    }

    def "should add operation to database"() {
        when: "a new operation is created"
        FilePart filePart = loadZipFile()
        def operationId = facade.createOperation(filePart).block()

        and: "the operation is retrieved from the database"
        def operation = operationFacade.getOperation(operationId).block()

        then: "operation data is added to database"
        operation != null
        operation.name == "Cochlear-Operation"
        operation.subprocesses*.id.size() == 5
        operation.createdAt != null
    }

    def "should add subprocesses to database"() {
        given: "a new operation is created"
        FilePart filePart = loadZipFile()
        def operationId = facade.createOperation(filePart).block()
        def operation = operationFacade.getOperation(operationId).block()

        when: "subprocesses are added to the operation"
        def subprocesses = operationFacade.getSubprocesses(operation.subprocesses*.id).collectList().block()

        then: "the subprocesses are retrieved from the database"
        subprocesses.size() == 5
        subprocesses[0].name == "Vorbereitung"
        subprocesses[1].name == "Zugang"
        subprocesses[2].name == "Operation unter Mikroskop"
        subprocesses[3].name == "Operation nach Mikroskop"
        subprocesses[4].name == "Nachbereitung"
    }

    def "should add elements to database"() {
        given: "a new operation is created"
        FilePart filePart = loadZipFile()
        def operationId = facade.createOperation(filePart).block()
        def operation = operationFacade.getOperation(operationId).block()

        and: "the subprocesses are retrieved from the database"
        def subprocesses = operationFacade.getSubprocesses(operation.subprocesses*.id).collectList().block()
        def elementIds = subprocesses.collectMany { it.elementIds }

        when: "elements are added to the operation"
        def elements = operationFacade.getElements(elementIds).collectList().block()

        then: "the elements are retrieved from the database"
        elements.size() == 171
    }

    def "should add meta data to database"() {
        given: "a new operation is created"
        FilePart filePart = loadZipFile()
        def operationId = facade.createOperation(filePart).block()
        def operation = operationFacade.getOperation(operationId).block()

        and: "the subprocesses are retrieved from the database"
        def subprocesses = operationFacade.getSubprocesses(operation.subprocesses*.id).collectList().block()
        def elementIds = subprocesses.collectMany { it.elementIds }

        when: "elements are added to the operation"
        def metaData = operationFacade.getMetaDataByElementIds(elementIds).collectList().block()

        then: "the elements are retrieved from the database"
        metaData.size() == 19
    }

    def "should add phrases to database"() {
        given: "a new operation is created"
        FilePart filePart = loadZipFile()
        def operationId = facade.createOperation(filePart).block()
        def operation = operationFacade.getOperation(operationId).block()

        and: "the subprocesses are retrieved from the database"
        def subprocesses = operationFacade.getSubprocesses(operation.subprocesses*.id).collectList().block()
        def elementIds = subprocesses.collectMany { it.elementIds }

        when: "phrases are added to the operation"
        def phrases = operationFacade.getPhrasesByElementIds(elementIds).collectList().block()

        then: "the phrases are retrieved from the database"
        phrases.size() == 71
    }

    def "should add images to database"() {
        given: "a new operation is created"
        FilePart filePart = loadZipFile()
        def operationId = facade.createOperation(filePart).block()
        def operation = operationFacade.getOperation(operationId).block()

        and: "the subprocesses are retrieved from the database"
        def subprocesses = operationFacade.getSubprocesses(operation.subprocesses*.id).collectList().block()
        def elementIds = subprocesses.collectMany { it.elementIds }

        and: "meta data is retrieved"
        def metaData = operationFacade.getMetaDataByElementIds(elementIds).collectList().block()

        when: "images are retrieved from the database"
        println metaData
        def allImageIds = metaData.collectMany { metaDataItem ->
            def imageIds = []
            if (metaDataItem.surgeonData != null) {
                imageIds += metaDataItem.surgeonData.imageIds
            }
            if (metaDataItem.studentData != null) {
                imageIds += metaDataItem.studentData.imageIds
            }
            return imageIds
        }

        println new HashSet(allImageIds)

        def images = operationFacade.getImages(new HashSet(allImageIds)).collectList().block()

        then: "the images are retrieved from the database"
        images.size() == 1
    }

    def "should get operation for all roles"() {
        given: "a new operation is created"
        FilePart filePart = loadZipFile()
        def operationId = facade.createOperation(filePart).block()

        when: "the surgeon operation is retrieved by operation id"
        def operation = operationFacade.getOperation(operationId).block()

        then: "it should have all properties of the operation"
        operation != null
        operation.name == "Cochlear-Operation"
        operation.subprocesses.size() == 5

        operation.subprocesses[0].elements.size()
        operation.subprocesses[0].name == "Vorbereitung"

        operation.subprocesses[1].elements.size()
        operation.subprocesses[1].name == "Zugang"

        operation.subprocesses[2].elements.size()
        operation.subprocesses[2].name == "Operation unter Mikroskop"

        operation.subprocesses[3].elements.size()
        operation.subprocesses[3].name == "Operation nach Mikroskop"

        operation.subprocesses[4].elements.size()
        operation.subprocesses[4].name == "Nachbereitung"

        operation.subprocesses*.elements.flatten().size() == 171
        operation.createdAt != null

        operation.subprocesses[2].elements[2].surgeonImages.size() == 0
        operation.subprocesses[2].elements[2].surgeonInfo == null

        operation.subprocesses[2].elements[2].studentImages.size() == 1
        operation.subprocesses[2].elements[2].studentInfo != null

        operation.subprocesses[2].elements[2].instruments == null
    }

    private FilePart loadZipFile() {
        def zipFile = new File("src/test/resources/Beispiel.zip")
        def mockMultipartFile = new MockMultipartFile("file", zipFile.getName(), "application/zip", zipFile.bytes)
        new FilePart() {
            @Override
            String name() {
                return mockMultipartFile.getName()
            }

            @Override
            String filename() {
                return mockMultipartFile.getOriginalFilename()
            }

            @Override
            Mono<Void> transferTo(Path dest) {
                return Mono.fromRunnable {
                    zipFile.withInputStream { is ->
                        dest.toFile().withOutputStream { os ->
                            os << is
                        }
                    }
                }
            }

            @Override
            HttpHeaders headers() {
                HttpHeaders headers = new HttpHeaders()
                headers.setContentType(MediaType.valueOf(mockMultipartFile.getContentType()))
                headers.setContentLength(mockMultipartFile.getSize())
                return headers
            }

            @Override
            Flux<DataBuffer> content() {
                return DataBufferUtils.read(zipFile.toPath(), new DefaultDataBufferFactory(), 4096)
            }
        }
    }
}
