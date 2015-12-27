package io.muoncore.protocol.requestresponse
import io.muoncore.channel.Channels
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocol
import io.muoncore.protocol.support.ProtocolTimer
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportOutboundMessage
import reactor.Environment
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class RequestResponseClientProtocolSpec extends Specification {

    def "protocol sends a TransportOutboundMessage for every request made"() {

        Environment.initializeIfEmpty()
        def ret

        def leftChannel = Channels.channel("left", "right")
        def rightChannel = Channels.channel("left", "right")

        rightChannel.right().receive({
            ret = it
        })

        def proto = new RequestResponseClientProtocol(
                "tombola",
                leftChannel.right(),
                rightChannel.left(),
                Map,
                new JsonOnlyCodecs(), new ProtocolTimer())

        when:
        leftChannel.left().send(new Request(new RequestMetaData("url","service", "remote"),[:]))

        then:
        new PollingConditions().eventually {
            ret instanceof TransportOutboundMessage
            ret.protocol == RRPTransformers.REQUEST_RESPONSE_PROTOCOL
        }
    }
    def "when recieving a ServiceNotFound, returns a 404 response"() {

        Environment.initializeIfEmpty()
        Response ret

        def leftChannel = Channels.channel("left", "right")
        def rightChannel = Channels.channel("left", "right")

        leftChannel.left().receive({
            ret = it
        })

        def proto = new RequestResponseClientProtocol(
                "tombola",
                leftChannel.right(),
                rightChannel.left(),
                Map,
                new JsonOnlyCodecs(), new ProtocolTimer())

        when:
        rightChannel.right().send(TransportInboundMessage.serviceNotFound(
                new TransportOutboundMessage(
                        "Meh",
                        "",
                        "simples",
                        "tombola",
                        RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                        [:],
                        "",
                        null,
                        null
                )
        ))

        then:
        new PollingConditions().eventually {
            ret instanceof Response
            ret.status==404
        }
    }
}
