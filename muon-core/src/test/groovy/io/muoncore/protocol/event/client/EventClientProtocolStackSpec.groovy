package io.muoncore.protocol.event.client
import io.muoncore.Discovery
import io.muoncore.ServiceDescriptor
import io.muoncore.channel.ChannelConnection
import io.muoncore.channel.async.StandardAsyncChannel
import io.muoncore.codec.Codecs
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.protocol.ChannelFunctionExecShimBecauseGroovyCantCallLambda
import io.muoncore.protocol.event.Event
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportMessage
import io.muoncore.transport.client.TransportClient
import spock.lang.Specification
import spock.lang.Timeout
import spock.util.concurrent.PollingConditions

@Timeout(5)
class EventClientProtocolStackSpec extends Specification {

    def "Stack converts events to transport messages"() {

        StandardAsyncChannel.echoOut=true

        def capturedFunction
        def config = new AutoConfiguration(serviceName: "tombola")
        def discovery = Mock(Discovery) {
            findService(_) >> Optional.of(new ServiceDescriptor("tombola", [], [], []))
        }

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

            @Override
            AutoConfiguration getConfiguration() {
                return config
            }
        }

        when:
        def future = eventProto.event(new Event("SomethingHappened", "simples", "myParent", "myService", []))

        and: "A response comes back from the remote"
        Thread.start {
            Thread.sleep(100)
            capturedFunction(new TransportInboundMessage(
                    "response",
                    "id",
                    "targetService",
                    "sourceServiceName",
                    "fakeproto",
                    ["status":"200"],
                    "text/plain",
                    new byte[0], [], TransportMessage.ChannelOperation.NORMAL))
        }

        sleep(200)

        then:
        capturedFunction != null
        1 * clientChannel.send(_ as TransportMessage)
        1 * clientChannel.send(null)
        new PollingConditions().eventually {
            future.get() instanceof EventResult
            future.get().status == EventResult.EventResultStatus.PERSISTED
        }
    }

    def "Stack sends all with the event protocol set"() {

        def discovery = Mock(Discovery) {
            findService(_) >> Optional.of(new ServiceDescriptor("tombola", [], [], []))
        }
        def config = new AutoConfiguration(serviceName: "tombola")

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

            @Override
            AutoConfiguration getConfiguration() {
                return config
            }
        }

        when:
        eventProto.event(new Event("SomethingHappened", "simples", "myParent", "myService", []))
        sleep(50)

        then:
        1 * clientChannel.send({ it.protocol == "event" })
    }

    def "Sends a 404 response if no eventstore service found"() {

        def discovery = Mock(Discovery) {
            findService(_) >> Optional.empty()
        }
        def config = new AutoConfiguration(serviceName: "tombola")

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

            @Override
            AutoConfiguration getConfiguration() {
                return config
            }
        }

        when:
        def response = eventProto.event(new Event("SomethingHappened2", "simples", "myParent", "myService", [])).get()

        then:
        response
        response.status == EventResult.EventResultStatus.FAILED
    }
}
