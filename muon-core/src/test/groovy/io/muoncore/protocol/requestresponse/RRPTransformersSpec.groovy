package io.muoncore.protocol.requestresponse

import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportOutboundMessage
import spock.lang.Specification

class RRPTransformersSpec extends Specification {

    def "TransportInboundMessage to Request "() {
        when:
        def ret = RRPTransformers.toRequest(inbound())

        then:
        ret.id == request().id
    }

    def "TransportInboundMessage to response"() {
        when:
        def ret = RRPTransformers.toResponse(inbound())

        then:
        ret.id == response().id

    }

    def "Request to TransportOutboundMessage"() {
        when:
        def ret = RRPTransformers.toOutbound(request())

        then:
        ret.id == outbound().id
    }

    def "Response to TransportOutboundMessage"() {
        when:
        def ret = RRPTransformers.toOutbound(response())

        then:
        ret.id == outbound().id
    }

    Request request() {
        new Request(id: "1234")
    }
    Response response() {
        new Response("1234", "helloworld")
    }

    TransportInboundMessage inbound() {
        new TransportInboundMessage("1234", "myservice", "requestresponse")
    }

    TransportOutboundMessage outbound() {
        new TransportOutboundMessage("1234", "myservice", "requestresponse")
    }

}
