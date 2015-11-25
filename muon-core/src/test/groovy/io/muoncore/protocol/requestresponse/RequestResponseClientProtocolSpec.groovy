package io.muoncore.protocol.requestresponse
import io.muoncore.channel.Channels
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocol
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
                new JsonOnlyCodecs())

        when:
        leftChannel.left().send(new Request(new RequestMetaData("url","service", "remote"),[:]))

        then:
        new PollingConditions().eventually {
            ret instanceof TransportOutboundMessage
            ret.protocol == RRPTransformers.REQUEST_RESPONSE_PROTOCOL
        }
    }
}
