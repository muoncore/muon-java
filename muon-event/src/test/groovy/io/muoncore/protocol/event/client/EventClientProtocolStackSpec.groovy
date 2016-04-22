package io.muoncore.protocol.event.client

import io.muoncore.Discovery
import io.muoncore.Muon
import io.muoncore.ServiceDescriptor
import io.muoncore.api.MuonFuture
import io.muoncore.channel.ChannelConnection
import io.muoncore.channel.async.StandardAsyncChannel
import io.muoncore.codec.Codecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.descriptors.ProtocolDescriptor
import io.muoncore.descriptors.ServiceExtendedDescriptor
import io.muoncore.protocol.ChannelFunctionExecShimBecauseGroovyCantCallLambda
import io.muoncore.protocol.event.ClientEvent
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessage
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

        def muon = Mock(Muon) {
            getTransportClient() >> transportClient
            getDiscovery() >> discovery
            getConfiguration() >> config
            getCodecs() >> Mock(Codecs) {
                getAvailableCodecs() >> ([] as String[])
                encode(_, _) >> new Codecs.EncodingResult(new byte[0], "application/json")
                decode(_, _, _ ) >> new EventResult(EventResult.EventResultStatus.PERSISTED, "")
            }
            introspect(_) >> Mock(MuonFuture) {
                get() >> new ServiceExtendedDescriptor("tombola", [new ProtocolDescriptor("event", "", "", [])])
            }
        }

        def evClient = new DefaultEventClient(muon)

        when:
        def future = evClient.event(
                new ClientEvent("awesome", "SomethingHappened", "simples", 1234, "myService", []))

        and: "A response comes back from the remote"
        Thread.start {
            Thread.sleep(100)
            capturedFunction(new MuonInboundMessage(
                    "response",
                    "localId",
                    "targetService",
                    "sourceServiceName",
                    "fakeproto",
                    ["status":"200"],
                    "text/plain",
                    new byte[0], [], MuonMessage.ChannelOperation.normal))
        }

        sleep(200)

        then:
        capturedFunction != null
        1 * clientChannel.send(_ as MuonMessage)
        1 * clientChannel.send(null)
        new PollingConditions().eventually {
            future.get() instanceof EventResult
            future.get().status == EventResult.EventResultStatus.PERSISTED
        }

        cleanup:
        StandardAsyncChannel.echoOut = false
    }

    def "Stack sends all with the event protocol set"() {

        StandardAsyncChannel.echoOut=true
        def config = new AutoConfiguration(serviceName: "tombola")

        def discovery = Mock(Discovery) {
            findService(_) >> Optional.of(new ServiceDescriptor("tombola", [], [], []))
        }

        def clientChannel = Mock(ChannelConnection)
        def transportClient = Mock(TransportClient) {
            openClientChannel() >> clientChannel
        }

        def muon = Mock(Muon) {
            getTransportClient() >> transportClient
            getDiscovery() >> discovery
            getConfiguration() >> config
            getCodecs() >> Mock(Codecs) {
                getAvailableCodecs() >> ([] as String[])
                encode(_, _) >> new Codecs.EncodingResult(new byte[0], "application/json")
            }
        }

        def eventStore = new DefaultEventClient(muon)

        when:
        eventStore.event(
                new ClientEvent("awesome", "SomethingHappened", "simples", 1234, "myService", []))
        sleep(50)

        then:
        1 * clientChannel.send({ it.protocol == "event" })

        cleanup:
        StandardAsyncChannel.echoOut=false
    }

    def "Sends a 404 response if no eventstore service found"() {

        StandardAsyncChannel.echoOut=true

        def discovery = Mock(Discovery) {
            findService(_) >> Optional.empty()
        }
        def clientChannel = Mock(ChannelConnection)
        def transportClient = Mock(TransportClient) {
            openClientChannel() >> clientChannel
        }

        def muon = Mock(Muon) {
            getTransportClient() >> transportClient
            getDiscovery() >> discovery
        }

        def eventStore = new DefaultEventClient(muon)

        when:
        def response = eventStore.event(
                new ClientEvent("awesome", "SomethingHappened2", "simples", 1234, "myService", []))

        then:
        response
        response.status == EventResult.EventResultStatus.FAILED

        cleanup:
        StandardAsyncChannel.echoOut=false
    }

    def muon() {
        Muon
    }
}
