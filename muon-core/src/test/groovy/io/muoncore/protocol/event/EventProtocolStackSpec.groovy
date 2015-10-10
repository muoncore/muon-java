package io.muoncore.protocol.event

import io.muoncore.channel.ChannelConnection
import io.muoncore.protocol.ChannelFunctionExecShim
import io.muoncore.protocol.event.client.EventClientProtocolStack
import io.muoncore.protocol.requestresponse.Response
import io.muoncore.transport.client.TransportClient
import io.muoncore.transport.TransportInboundMessage
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class EventProtocolStackSpec extends Specification {

    def "Stack converts events to transport messages"() {

        def capturedFunction

        def clientChannel = Mock(ChannelConnection) {
            receive(_) >> { func ->
                capturedFunction = new ChannelFunctionExecShim(func[0])
            }
        }

        def transportClient = Mock(TransportClient) {
            openClientChannel() >> clientChannel
        }

        def eventProto = new EventClientProtocolStack() {
            @Override
            TransportClient getTransportClient() {
                return transportClient
            }
        }

        when:
        def future = eventProto.event(new Event("simples", "myParent", "myService", []))

        Thread.start {
            Thread.sleep(100)
            capturedFunction(new TransportInboundMessage("myId", "simples", "myChannel"))
        }

        then:
        capturedFunction != null
        1 * clientChannel.send(_)
        new PollingConditions().eventually {
            future.get() instanceof Response
        }
    }
}
