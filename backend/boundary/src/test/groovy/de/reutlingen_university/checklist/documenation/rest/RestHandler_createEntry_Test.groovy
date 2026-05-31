package de.reutlingen_university.checklist.documenation.rest


import de.reutlingen_university.checklist.documentation.CreateEntryReq
import reactor.core.publisher.Mono

import java.time.Instant

class RestHandler_createEntry_Test extends RestHandlerSpec {

    def "should be able to create entry"() {
        when: "the route is called"
        def startedAt = Instant.now()
        def finishedAt = Instant.now()
        CreateEntryReq r = new CreateEntryReq(
                "elementid",
                "description",
                "text",
                startedAt,
                finishedAt
        )
        def response = callRoute(r, "documentationId")

        then: "the response status code is 200"
        response.expectStatus().isEqualTo(200)

        and: "the facade method is called"
        1 * facade.createOrUpdateEntry(_ as String,_ as String, _ as CreateEntryReq) >> { String documentationId, String roomId,  CreateEntryReq req ->
            assert documentationId == "documentationId"
            assert roomId == "roomId"
            assert req.elementId == "elementid"
            assert req.description == "description"
            assert req.startedAt instanceof Instant
            assert req.finishedAt instanceof Instant

            return Mono.empty()
        }
    }

    def callRoute(CreateEntryReq req, String documentationId, String roomId = "roomId"){
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/documentation/${documentationId}/room/${roomId}/entry/create")
                        .build())
                .bodyValue(req)
                .exchange()
    }
}
