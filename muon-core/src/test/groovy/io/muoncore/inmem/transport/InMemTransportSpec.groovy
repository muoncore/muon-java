package io.muoncore.inmem.transport
import com.google.common.eventbus.EventBus
import io.muoncore.Discovery
import io.muoncore.codec.Codecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.transport.InMemClientChannelConnection
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.memory.transport.OpenChannelEvent
import io.muoncore.protocol.ServerStacks
import io.muoncore.protocol.requestresponse.RRPTransformers
import spock.lang.Specification

class InMemTransportSpec extends Specification {

    Codecs codecs = Mock(Codecs) {
        encode(_, _) >> new Codecs.EncodingResult(new byte[0], "application/json")
    }

    def "returns client channel connection on demand."() {
        def eventbus = new EventBus()
        def serverStacks = Mock(ServerStacks)

        def transport = new InMemTransport(new AutoConfiguration(serviceName: "tombola"), eventbus)
        transport.start(Mock(Discovery), serverStacks, codecs)

        when:
        def ret = transport.openClientChannel("tombola", RRPTransformers.REQUEST_RESPONSE_PROTOCOL)

        then:
        ret instanceof InMemClientChannelConnection
    }

    def "transport listens on event bus for OpenChannelEvents. opens channels in response"() {
        def eventbus = new EventBus()
        def serverStacks = Mock(ServerStacks)
        def clientConnection = Mock(InMemClientChannelConnection)

        def transport = new InMemTransport(new AutoConfiguration(serviceName: "tombola"), eventbus)
        transport.start(Mock(Discovery), serverStacks, codecs)

        when:
        eventbus.post(new OpenChannelEvent(
                "tombola", RRPTransformers.REQUEST_RESPONSE_PROTOCOL, clientConnection))

        then:
        1 * serverStacks.openServerChannel(RRPTransformers.REQUEST_RESPONSE_PROTOCOL)
    }
}
