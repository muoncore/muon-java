package io.muoncore.protocol.requestresponse

import io.muoncore.channel.async.StandardAsyncChannel
import io.muoncore.codec.JsonOnlyCodecs
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocol
import io.muoncore.transport.TransportOutboundMessage
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class RequestResponseClientProtocolSpec extends Specification {

    def "protocol sends a TransportOutboundMessage for every request made"() {

        def ret

        def leftChannel = new StandardAsyncChannel()
        def rightChannel = new StandardAsyncChannel()

        rightChannel.right().receive({
            ret = it
        })

        def proto = new RequestResponseClientProtocol(
                leftChannel.right(),
                rightChannel.left(),
                new JsonOnlyCodecs())

        when:
        leftChannel.left().send(new Request(id:"simples"))

        then:
        new PollingConditions().eventually {
            ret instanceof TransportOutboundMessage
            ret.id == "simples"
        }
    }
}
