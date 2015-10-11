package io.muoncore.protocol

import io.muoncore.channel.ChannelConnection
import spock.lang.Specification
import spock.lang.Unroll

class DynamicRegistrationServerProtocolsSpec extends Specification {

    def protos
    def defaultproto = Mock(ServerProtocol)
    def proto2 = Mock(ServerProtocol)
    def proto3 = Mock(ServerProtocol)

    def setup() {

        protos = new DynamicRegistrationServerProtocols(defaultproto)
        protos.registerServerProtocol("simples", proto2)
        protos.registerServerProtocol("wibble", proto3)
    }

    def "obtains channel from default ServerProtocol when no other match"() {

        def defaultproto = Mock(ServerProtocol)

        def protos = new DynamicRegistrationServerProtocols(defaultproto)

        when:
        def ret = protos.openServerChannel("nothinghere")

        then:
        ret != null
        1 * defaultproto.createChannel() >> Mock(ChannelConnection)
    }

    @Unroll
    def "when protocol name is #protoName, invoke #chosenProto"(chosenProto, protoName) {

        when:
        protos.openServerChannel(protoName)

        then:
        1 * getProperty(chosenProto).createChannel()

        where:

        chosenProto  || protoName
        "proto2"       || "simples"
        "defaultproto" || "NOTHING-DEFAULTME"
        "proto3"       || "wibble"
    }
}
