package io.muoncore.protocol.defaultproto

import io.muoncore.Discovery
import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.Codecs
import io.muoncore.config.MuonConfigBuilder
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonOutboundMessage
import spock.lang.Specification

class DefaultServerProtocolSpec extends Specification {

    def "default proto sends bounce messages and closes the connection"() {

        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
            encode(_, _) >> new Codecs.EncodingResult(new byte[0], "text/plain")
        }
        def disco = Mock(Discovery)
        def config = MuonConfigBuilder.withServiceIdentifier("simples").build()
        def proto = new DefaultServerProtocol(codecs, config, disco)
        def channel = proto.createChannel()
        def receive = Mock(ChannelConnection.ChannelFunction)

        channel.receive(receive)

        when:
        channel.send(MuonMessageBuilder.fromService("tombola").buildInbound())

        then:
        1 * receive.apply(_ as MuonOutboundMessage)
        1 * receive.apply(null)
    }
}
