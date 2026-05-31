package de.reutlingen_university.checklist.documenation.rest


import de.reutlingen_university.checklist.documentation.CreateDocumentationReq
import reactor.core.publisher.Mono

class RestHandler_createDocumentation_Test extends RestHandlerSpec {

    def "should be able to create documentation"() {
        when: "the route is called"
        CreateDocumentationReq r = new CreateDocumentationReq(
                "operationid",
                "ROOM1"
        )
        def response = callRoute(r)

        then: "the response status code is 200"
        response.expectStatus().isEqualTo(200)

        and: "the facade method is called"
        1 * facade.createDocumentation(_ as CreateDocumentationReq) >> { CreateDocumentationReq req ->
            assert req.operationId == "operationid"
            assert req.roomId == "ROOM1"
            return Mono.empty()
        }
    }

    def callRoute(CreateDocumentationReq req) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/documentation/create")
                        .build())
                .bodyValue(req)
                .exchange()
    }
}
