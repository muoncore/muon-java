package io.muoncore.protocol.requestresponse

import io.muoncore.codec.Codecs
import io.muoncore.codec.GsonCodec
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportOutboundMessage
import spock.lang.Specification

class RRPTransformersSpec extends Specification {

    Codecs codecs = Mock(Codecs) {
        encode(_,_) >> new Codecs.EncodingResult(new byte[0], "text/plain")
    }

    def "TransportInboundMessage to Request "() {


        when:
        def ret = RRPTransformers.toRequest(inboundRequest(), codecs, Map)

        then:
        ret.metaData[(Request.URL)] == "hello"
    }

    def "TransportInboundMessage to response"() {
        when:
        def ret = RRPTransformers.toResponse(inbound(), codecs, Map)

        then:
        ret.status == 200

    }

    def "Request to TransportOutboundMessage"() {
        when:
        def ret = RRPTransformers.toOutbound("myservice", request(), codecs, ["application/json"] as String[])

        then:
        ret.contentType == "text/plain"
        ret.metadata[(Request.URL)] == "simples"
    }

    def "Response to TransportOutboundMessage"() {
        when:
        def ret = RRPTransformers.toOutbound("myservice", response(), codecs, ["application/json"] as String[])

        then:
        ret.metadata[(Response.STATUS)] == "200"
    }

    Request request() {
        new Request(new RequestMetaData("simples", "myservice", "remote"), [:])
    }
    Response response() {
        new Response(200, [message:"hello"])
    }

    TransportInboundMessage inbound() {
        new TransportInboundMessage(
                "somethingHappened",
                "1234",
                "remoteService",
                "myservice",
                RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                [(Response.STATUS):"200"],
                "application/json",
                new GsonCodec().encode([:]))
    }

    TransportInboundMessage inboundRequest() {
        new TransportInboundMessage(
                "somethingHappened",
                "1234",
                "remoteService",
                "myservice",
                RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                [(Request.URL):"hello"],
                "application/json",
                new GsonCodec().encode([:]))
    }


    TransportOutboundMessage outbound() {
        new TransportOutboundMessage("somethingHappened","1234",
                "remoteService",
                "myservice",
                RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                [:],
                "application/json",
                new GsonCodec().encode([:]))
    }

}
