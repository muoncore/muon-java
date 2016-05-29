package io.muoncore.protocol.requestresponse

import io.muoncore.channel.Channels
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonOutboundMessage
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocol
import io.muoncore.channel.support.Scheduler
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
                new JsonOnlyCodecs(), new Scheduler())

        when:
        leftChannel.left().send(new Request(new URI("request://somewhere"),[:]))

        then:
        new PollingConditions().eventually {
            ret instanceof MuonOutboundMessage
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
                new JsonOnlyCodecs(), new Scheduler())

        when:
        rightChannel.right().send(MuonInboundMessage.serviceNotFound(
                MuonMessageBuilder
                        .fromService("tombole")
                        .toService("simples")
                        .step("Meh")
                        .protocol(RRPTransformers.REQUEST_RESPONSE_PROTOCOL)
                        .contentType("application/json")
                        .payload()
                        .build()
        ))

        then:
        new PollingConditions().eventually {
            ret instanceof Response
            ret.status==404
        }
    }
}
