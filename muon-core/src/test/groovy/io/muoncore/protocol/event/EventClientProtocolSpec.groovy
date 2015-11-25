package io.muoncore.protocol.event
import io.muoncore.Discovery
import io.muoncore.ServiceDescriptor
import io.muoncore.channel.Channels
import io.muoncore.config.AutoConfiguration
import io.muoncore.protocol.event.client.EventClientProtocol
import io.muoncore.protocol.requestresponse.Request
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class EventClientProtocolSpec extends Specification {

    def "protocol sends a Request on for event Event"() {

        def discovery = Mock(Discovery) {
            findService(_) >> Optional.of(new ServiceDescriptor("tombola", [], [], []))
        }

        def leftChannel = Channels.channel("left", "right")
        def rightChannel = Channels.channel("left", "right")

        def ret

        rightChannel.right().receive({
            ret = it
        })

        def proto = new EventClientProtocol(
                new AutoConfiguration(serviceName: "tombola"),
                discovery,
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
