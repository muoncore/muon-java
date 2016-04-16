package io.muoncore.protocol.requestresponse

import io.muoncore.codec.Codecs
import io.muoncore.codec.json.GsonCodec
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonOutboundMessage
import spock.lang.Specification

class RRPTransformersSpec extends Specification {

    Codecs codecs = Mock(Codecs) {
        encode(_,_) >> new Codecs.EncodingResult(new byte[0], "text/plain")
        getAvailableCodecs() >> []
    }

    def "TransportInboundMessage to Request "() {


        when:
        def ret = RRPTransformers.toRequest(inboundRequest(), new JsonOnlyCodecs(), Map)

        then:
        ret.headers.url == "hello"
    }

    def "TransportInboundMessage to response"() {
        when:
        def ret = RRPTransformers.toResponse(inbound(), new JsonOnlyCodecs(), Map)

        then:
        ret.status == 200

    }

    def "Request to TransportOutboundMessage"() {
        when:
        def ret = RRPTransformers.toOutbound("myservice", request(), codecs, ["application/json"] as String[])

        then:
        ret.contentType == "text/plain"
        ret.step == RRPEvents.REQUEST
    }

    def "Response to TransportOutboundMessage"() {
        when:
        def ret = RRPTransformers.toOutbound("myservice", "targetService", response(), codecs, ["application/json"] as String[])

        then:
        ret.step == RRPEvents.RESPONSE
    }

    Request request() {
        new Request(new Headers("simples", "myservice", "remote"), [:])
    }
    Response response() {
        new Response(200, [message:"hello"])
    }

    MuonInboundMessage inbound() {
        MuonMessageBuilder.fromService("myservice")
            .toService("remoteService")
            .step("somethingHappened")
            .protocol(RRPTransformers.REQUEST_RESPONSE_PROTOCOL)
            .contentType("application/json")
            .payload(new GsonCodec().encode(new Response(200,[:]))).buildInbound()
    }

    MuonInboundMessage inboundRequest() {
        MuonMessageBuilder.fromService("myservice")
                .toService("remoteService")
                .step("somethingHappened")
                .protocol(RRPTransformers.REQUEST_RESPONSE_PROTOCOL)
                .contentType("application/json")
                .payload(new GsonCodec().encode(
                new Request(new Headers("hello", "sourceService", "targetService"), [:])
        )).buildInbound()
    }


    MuonOutboundMessage outbound() {
        MuonMessageBuilder.fromService("myservice")
                .toService("remoteService")
                .step("somethingHappened")
                .protocol(RRPTransformers.REQUEST_RESPONSE_PROTOCOL)
                .contentType("application/json")
                .payload(new GsonCodec().encode([:])).build()
    }

}
