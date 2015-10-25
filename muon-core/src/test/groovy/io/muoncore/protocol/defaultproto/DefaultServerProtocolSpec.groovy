package io.muoncore.protocol.defaultproto

import io.muoncore.channel.ChannelConnection
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportOutboundMessage
import spock.lang.Specification

class DefaultServerProtocolSpec extends Specification {

    def "default proto sends bounce messages"() {

        def proto = new DefaultServerProtocol()
        def channel = proto.createChannel()
        def receive = Mock(ChannelConnection.ChannelFunction)

        channel.receive(receive)

        when:
        channel.send(new TransportInboundMessage(
                "somethingHappened",
                "id",
                "targetService",
                "sourceServiceName",
                "fakeproto",
                [:],
                "text/plain",
                new byte[0]))

        then:
        1 * receive.apply(_ as TransportOutboundMessage)
    }
}
