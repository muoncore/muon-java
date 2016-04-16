package io.muoncore.protocol.requestresponse

import io.muoncore.codec.Codecs
import io.muoncore.codec.json.GsonCodec
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessage
import io.muoncore.message.MuonOutboundMessage
import spock.lang.Specification

class RRPTransformersSpec extends Specification {

    Codecs codecs = Mock(Codecs) {
        encode(_,_) >> new Codecs.EncodingResult(new byte[0], "text/plain")
        getAvailableCodecs() >> []
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
        def ret = RRPTransformers.toOutbound("myservice", "targetService", response(), codecs, ["application/json"] as String[])

        then:
        ret.metadata[(Response.STATUS)] == "200"
    }

    Request request() {
        new Request(new RequestMetaData("simples", "myservice", "remote"), [:])
    }
    Response response() {
        new Response(200, [message:"hello"])
    }

    MuonInboundMessage inbound() {
        new MuonInboundMessage(
                "somethingHappened",
                "1234",
                "remoteService",
                "myservice",
                RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                [(Response.STATUS):"200"],
                "application/json",
                new GsonCodec().encode([:]), ["application/json"], MuonMessage.ChannelOperation.NORMAL)
    }

    MuonInboundMessage inboundRequest() {
        new MuonInboundMessage(
                "somethingHappened",
                "1234",
                "remoteService",
                "myservice",
                RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                [(Request.URL):"hello"],
                "application/json",
                new GsonCodec().encode([:]), ["application/json"], MuonMessage.ChannelOperation.NORMAL)
    }


    MuonOutboundMessage outbound() {
        new MuonOutboundMessage("somethingHappened","1234",
                "remoteService",
                "myservice",
                RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                [:],
                "application/json",
                new GsonCodec().encode([:]), ["application/json"])
    }

}
