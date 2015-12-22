package io.muoncore.protocol.event.client
import io.muoncore.Discovery
import io.muoncore.ServiceDescriptor
import io.muoncore.channel.Channels
import io.muoncore.codec.Codecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.protocol.event.Event
import io.muoncore.transport.TransportOutboundMessage
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class EventClientProtocolSpec extends Specification {

    def "protocol sends an event on for event Event"() {

        def discovery = Mock(Discovery) {
            findService(_) >> Optional.of(new ServiceDescriptor("tombola", [], [], []))
        }
        def codecs = Mock(Codecs) {
            encode(_, _) >> new Codecs.EncodingResult(null, null)
            getAvailableCodecs() >> []
        }

        def leftChannel = Channels.channel("left", "right")
        def rightChannel = Channels.channel("left", "right")

        def ret

        rightChannel.right().receive({
            ret = it
        })

        def proto = new EventClientProtocol(
                new AutoConfiguration(serviceName: "tombola"),
                discovery, codecs,
                leftChannel.right(), rightChannel.left())

        when:
        leftChannel.left().send(new Event(
                "SomethingHappened",
                "awesome",
                "parentId",
                "serviceId",
                ["1":2, "payload":true]
        ))

        then:
        new PollingConditions().eventually {
            ret instanceof TransportOutboundMessage
            ret.id == "awesome"
        }
    }
}
