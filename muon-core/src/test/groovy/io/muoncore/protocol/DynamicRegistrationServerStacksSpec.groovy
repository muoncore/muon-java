package io.muoncore.protocol

import io.muoncore.channel.ChannelConnection
import io.muoncore.descriptors.ProtocolDescriptor
import io.muoncore.transport.client.TransportMessageDispatcher
import spock.lang.Specification
import spock.lang.Unroll

class DynamicRegistrationServerStacksSpec extends Specification {

    TransportMessageDispatcher tap = Mock(TransportMessageDispatcher)

    def protos
    def defaultproto = Mock(ServerProtocolStack) {
        getProtocolDescriptor() >> new ProtocolDescriptor("default", "Default Protocol", "Returns 404 for all messages that match no other protocol", Collections.emptyList());
    }
    def proto2 = Mock(ServerProtocolStack) {
        getProtocolDescriptor() >> new ProtocolDescriptor("simples", "Default Protocol", "Returns 404 for all messages that match no other protocol", Collections.emptyList());
    }
    def proto3 = Mock(ServerProtocolStack) {
        getProtocolDescriptor() >> new ProtocolDescriptor("wibble", "Default Protocol", "Returns 404 for all messages that match no other protocol", Collections.emptyList());
    }

    def setup() {

        protos = new DynamicRegistrationServerStacks(defaultproto, tap)
        protos.registerServerProtocol(proto2)
        protos.registerServerProtocol(proto3)
    }

    def "returns list of protocol descriptors"() {
        def defaultproto = Mock(ServerProtocolStack)

        def protos = new DynamicRegistrationServerStacks(defaultproto, tap)
        protos.registerServerProtocol(Mock(ServerProtocolStack) { getProtocolDescriptor() >> new ProtocolDescriptor("simple", "", "", [])})
        protos.registerServerProtocol(Mock(ServerProtocolStack) { getProtocolDescriptor() >> new ProtocolDescriptor("fake", "", "", [])})
        protos.registerServerProtocol(Mock(ServerProtocolStack) { getProtocolDescriptor() >> new ProtocolDescriptor("advance", "", "", [])})

        when:
        def descriptors = protos.protocolDescriptors

        then:
        descriptors.size() == 3
    }

    def "obtains channel from default ServerProtocol when no other match"() {

        def defaultproto = Mock(ServerProtocolStack)

        def protos = new DynamicRegistrationServerStacks(defaultproto, tap)

        when:
        def ret = protos.openServerChannel("simples")

        then:
        ret != null
        1 * defaultproto.createChannel() >> Mock(ChannelConnection)
    }

    @Unroll
    def "when protocol name is #protoName, invoke #chosenProto"(chosenProto, protoName) {

        when:
        protos.openServerChannel(protoName)

        then:
        1 * getProperty(chosenProto).createChannel() >> Mock(ChannelConnection)

        where:

        chosenProto  || protoName
        "proto2"       || "simples"
        "defaultproto" || "NOTHING-DEFAULTME"
        "proto3"       || "wibble"
    }

    def "allows tapping of inbound connections"() {

        def defaultproto = Mock(ServerProtocolStack)

        def protos = new DynamicRegistrationServerStacks(defaultproto, tap)

        when:
        def ret = protos.openServerChannel("simples")

        then:
        ret != null
        1 * defaultproto.createChannel() >> Mock(ChannelConnection)
    }
}
