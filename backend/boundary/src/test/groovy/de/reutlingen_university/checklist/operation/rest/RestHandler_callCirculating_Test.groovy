package de.reutlingen_university.checklist.operation.rest


import de.reutlingen_university.checklist.operation.CallCirculatingReq
import reactor.core.publisher.Mono

class RestHandler_callCirculating_Test extends RestHandlerSpec {

    def "should be able to call circulating nurse"() {
        when: "the route is called"
        CallCirculatingReq req = new CallCirculatingReq(
                "Room1",
                "DocumentationId",
                "Some message"
        )
        def response = callRoute(req)

        then: "the response status code is 200"
        response.expectStatus().isEqualTo(200)

        and: "the facade method is called"
        1 * facade.callCirculating(_ as CallCirculatingReq) >> { CallCirculatingReq r ->
            assert r.roomId == "Room1"
            assert r.description == "Some message"

            return Mono.empty()
        }
    }

    def callRoute(CallCirculatingReq req) {
        webClient.post()
                .uri("/sync/circulating/call")
                .bodyValue(req)
                .exchange()
    }
}
