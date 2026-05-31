package de.reutlingen_university.checklist.operation.rest


import reactor.core.publisher.Mono

class RestHandler_getOperation_Test extends RestHandlerSpec {

    def "should be able to get operation"() {
        when: "the route is called"
        def response = callRoute("operationId")

        then: "the response status code is 200"
        response.expectStatus().isEqualTo(200)

        and: "the facade method is called"
        1 * operationFacade.getOperation(_ as String) >> { String id ->
            assert id == "operationId"
            return Mono.empty()
        }
    }

    def callRoute(String operationId) {
        webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/operation/$operationId")
                        .build())
                .exchange()
    }
}
