package io.muoncore.protocol.event

import io.muoncore.Discovery
import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.Codecs
import io.muoncore.codec.JsonOnlyCodecs
import io.muoncore.protocol.ChannelFunctionExecShimBecauseGroovyCantCallLambda
import io.muoncore.protocol.event.client.EventClientProtocolStack
import io.muoncore.protocol.requestresponse.Response
import io.muoncore.transport.client.TransportClient
import io.muoncore.transport.TransportInboundMessage
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class EventClientProtocolStackSpec extends Specification {

    def "Stack converts events to transport messages"() {

        def capturedFunction
        def discovery = Mock(Discovery)

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
                discovery
            }

            @Override
            Codecs getCodecs() {
                return new JsonOnlyCodecs()
            }
        }

        when:
        def future = eventProto.event(new Event("simples", "myParent", "myService", []))

        Thread.start {
            Thread.sleep(100)
            capturedFunction(new TransportInboundMessage("myId", "simples", "myChannel"))
        }

        sleep(50)

        then:
        capturedFunction != null
        1 * clientChannel.send(_)
        new PollingConditions().eventually {
            future.get() instanceof Response
        }
    }

    def "Stack sends all with the event protocol set"() {

        def discovery = Mock(Discovery)

        def clientChannel = Mock(ChannelConnection)

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
                discovery
            }

            @Override
            Codecs getCodecs() {
                return new JsonOnlyCodecs()
            }
        }

        when:
        eventProto.event(new Event("simples", "myParent", "myService", []))
        sleep(50)

        then:
        1 * clientChannel.send({ it.protocol == "event" })
    }
}
