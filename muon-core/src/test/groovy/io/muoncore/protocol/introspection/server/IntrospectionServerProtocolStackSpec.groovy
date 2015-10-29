package io.muoncore.protocol.introspection.server
import io.muoncore.codec.Codecs
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.descriptors.ServiceExtendedDescriptor
import io.muoncore.descriptors.ServiceExtendedDescriptorSource
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportOutboundMessage
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class IntrospectionServerProtocolStackSpec extends Specification {

    def "responds with an introspection report"() {
        def descriptorSource = Mock(ServiceExtendedDescriptorSource) {
            getServiceExtendedDescriptor() >> new ServiceExtendedDescriptor("awesome", [])
        }
        def codecs = new JsonOnlyCodecs()

        def stack = new IntrospectionServerProtocolStack(descriptorSource, codecs)

        TransportOutboundMessage outbound

        def channel = stack.createChannel()
        channel.receive({
            outbound = it
        })

        when:
        channel.send(new TransportInboundMessage(
                "introspect",
                "simples",
                "someService",
                "myService",
                "introspect",
                [:],
                "application/json",
                [] as byte[],
                ["application/json"]
        ))

        then:
        new PollingConditions().eventually {
            outbound &&
                    codecs.decode(outbound.payload, "application/json", Map).serviceName == "awesome"
        }
    }

    def "generates protocol descriptor"() {

        def descriptorSource = Mock(ServiceExtendedDescriptorSource)
        def codecs = Mock(Codecs)

        def stack = new IntrospectionServerProtocolStack(descriptorSource, codecs)

        expect:
        stack.protocolDescriptor.operations.size() == 0
        stack.protocolDescriptor.protocolScheme == "introspect"
    }
}
