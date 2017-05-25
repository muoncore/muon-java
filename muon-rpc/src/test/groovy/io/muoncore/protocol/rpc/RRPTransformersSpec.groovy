package io.muoncore.protocol.rpc

import io.muoncore.codec.Codecs
import io.muoncore.codec.json.GsonCodec
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonOutboundMessage
import io.muoncore.protocol.rpc.server.ServerResponse
import spock.lang.Specification

class RRPTransformersSpec extends Specification {

    Codecs codecs = Mock(Codecs) {
        encode(_,_) >> new Codecs.EncodingResult(new byte[0], "text/plain")
        getAvailableCodecs() >> []
    }

    def "TransportInboundMessage to Request "() {


        when:
        def ret = RRPTransformers.toRequest(inboundRequest(), new JsonOnlyCodecs())

        then:
        ret.url == new URI("request://hello")
    }

    def "TransportInboundMessage to response"() {
        when:
        def ret = RRPTransformers.toResponse(inbound(), new JsonOnlyCodecs())

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
        new Request(new URI("request://something"), [:])
    }
    ServerResponse response() {
        new ServerResponse(200, [message:"hello"])
    }

    MuonInboundMessage inbound() {
        MuonMessageBuilder.fromService("myservice")
            .toService("remoteService")
            .step("somethingHappened")
            .protocol(RRPTransformers.REQUEST_RESPONSE_PROTOCOL)
            .contentType("application/json")
            .payload(new GsonCodec().encode(new ServerResponse(200,[:]))).buildInbound()
    }

    MuonInboundMessage inboundRequest() {
        MuonMessageBuilder.fromService("myservice")
                .toService("remoteService")
                .step("somethingHappened")
                .protocol(RRPTransformers.REQUEST_RESPONSE_PROTOCOL)
                .contentType("application/json")
                .payload(new GsonCodec().encode(
                new Request(new URI("request://hello"), [:])
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
