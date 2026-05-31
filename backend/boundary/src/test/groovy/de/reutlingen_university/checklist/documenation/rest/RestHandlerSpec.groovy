package de.reutlingen_university.checklist.documenation.rest


import de.reutlingen_university.checklist.documentation.DocumentationFacade
import de.reutlingen_university.checklist.documentation.rest.DocumentationRestHandler
import de.reutlingen_university.checklist.documentation.rest.DocumentationRestRoutingConfig
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@WebFluxTest
@ContextConfiguration(classes = [DocumentationRestHandler.class, DocumentationRestRoutingConfig.class])
class RestHandlerSpec extends Specification {

    @Autowired
    WebTestClient webClient

    @SpringBean
    DocumentationFacade facade = Mock()

}
