package io.muoncore.protocol.defaultproto

import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.Codecs
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessage
import io.muoncore.message.MuonOutboundMessage
import spock.lang.Specification

class DefaultServerProtocolSpec extends Specification {

    def "default proto sends bounce messages and closes the connection"() {

        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
        }
        def proto = new DefaultServerProtocol(codecs)
        def channel = proto.createChannel()
        def receive = Mock(ChannelConnection.ChannelFunction)

        channel.receive(receive)

        when:
        channel.send(new MuonInboundMessage(
                "somethingHappened",
                "id",
                "targetService",
                "sourceServiceName",
                "fakeproto",
                [:],
                "text/plain",
                new byte[0], ["application/json"], MuonMessage.ChannelOperation.NORMAL))

        then:
        1 * receive.apply(_ as MuonOutboundMessage)
        1 * receive.apply(null)
    }
}
