package io.muoncore.protocol.introspection.server

import io.muoncore.Discovery
import io.muoncore.codec.Codecs
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.descriptors.ServiceExtendedDescriptor
import io.muoncore.descriptors.ServiceExtendedDescriptorSource
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonOutboundMessage
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class IntrospectionServerProtocolStackSpec extends Specification {

    def discovery = Mock(Discovery)

    def "responds with an introspection report"() {
        def descriptorSource = Mock(ServiceExtendedDescriptorSource) {
            getServiceExtendedDescriptor() >> new ServiceExtendedDescriptor("awesome", [])
        }
        def codecs = new JsonOnlyCodecs()

        def stack = new IntrospectionServerProtocolStack(descriptorSource, codecs, discovery)

        MuonOutboundMessage outbound

        def channel = stack.createChannel()
        channel.receive({
            outbound = it
        })

        when:
        channel.send(
                MuonMessageBuilder
                        .fromService("tombola")
                        .toService("simples")
                        .step("Meh")
                        .protocol(IntrospectionServerProtocolStack.PROTOCOL)
                        .contentType("application/json")
                        .payload()
                        .buildInbound())

        then:
        new PollingConditions().eventually {
            outbound &&
                    codecs.decode(outbound.payload, "application/json", Map).serviceName == "awesome"
        }
    }

    def "generates protocol descriptor"() {

        def descriptorSource = Mock(ServiceExtendedDescriptorSource)
        def codecs = Mock(Codecs)

        def stack = new IntrospectionServerProtocolStack(descriptorSource, codecs, discovery)

        expect:
        stack.protocolDescriptor.operations.size() == 0
        stack.protocolDescriptor.protocolScheme == "introspect"
    }
}
