package io.muoncore.protocol.event

import io.muoncore.Discovery
import io.muoncore.channel.async.StandardAsyncChannel
import io.muoncore.protocol.event.client.EventClientProtocol
import io.muoncore.protocol.requestresponse.Request
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class EventClientProtocolSpec extends Specification {

    def "protocol sends a Request on for event Event"() {

        def leftChannel = new StandardAsyncChannel()
        def rightChannel = new StandardAsyncChannel()

        def ret

        rightChannel.right().receive({
            ret = it
        })

        def proto = new EventClientProtocol(
                Mock(Discovery),
                leftChannel.right(), rightChannel.left())

        when:
        leftChannel.left().send(new Event(
                "awesome",
                "parentId",
                "serviceId",
                ["1":2, "payload":true]
        ))

        then:
        new PollingConditions().eventually {
            ret instanceof Request
            ret.id == "awesome"
        }
    }
}
