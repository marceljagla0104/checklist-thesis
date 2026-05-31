package de.reutlingen_university.checklist.operation.rest

import de.reutlingen_university.checklist.operation.OperationFacade
import de.reutlingen_university.checklist.sync.SyncFacade
import de.reutlingen_university.checklist.sync.rest.SyncRestHandler
import de.reutlingen_university.checklist.sync.rest.SyncRestRoutingConfig
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@WebFluxTest
@ContextConfiguration(classes = [SyncRestHandler.class, SyncRestRoutingConfig.class, OperationRestHandler, OperationRestRoutingConfig.class])
class RestHandlerSpec extends Specification {

    @Autowired
    WebTestClient webClient

    @SpringBean
    OperationFacade operationFacade = Mock()

    @SpringBean
    SyncFacade facade = Mock()
}
