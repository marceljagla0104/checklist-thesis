package de.reutlingen_university.checklist.operation.rest

import reactor.core.publisher.Flux

class RestHandler_getOperations_Test extends RestHandlerSpec {

    def "should be able to get operations"() {
        when: "the route is called"
        def response = callRoute()

        then: "the response status code is 200"
        response.expectStatus().isEqualTo(200)

        and: "the facade method is called"
        1 * operationFacade.getOperations() >> Flux.empty()
    }

    def callRoute() {
        webClient.get()
                .uri("/operations")
                .exchange()
    }
}
