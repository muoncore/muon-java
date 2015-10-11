package io.muoncore.protocol.event

import io.muoncore.Discovery
import io.muoncore.channel.ChannelConnection
import io.muoncore.protocol.ChannelFunctionExecShimBecauseGroovyCantCallLambda
import io.muoncore.protocol.event.client.EventClientProtocolStack
import io.muoncore.protocol.requestresponse.Response
import io.muoncore.transport.client.TransportClient
import io.muoncore.transport.TransportInboundMessage
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class EventClientProtocolStackSpec extends Specification {

    def "things unsure about"() {
        expect:
        throw new IllegalStateException("""
WHere do we set the 'protocol' in the transportoutbound?
logically here it should be the request/responseclientproto. this kind of sucks?""")
    }


    def "Stack converts events to transport messages"() {

        def capturedFunction

        def clientChannel = Mock(ChannelConnection) {
            receive(_) >> { func ->
                capturedFunction = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(func[0])
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

            @Override
            Discovery getDiscovery() {
                throw new IllegalStateException("Not implemented here")
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
