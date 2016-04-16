package io.muoncore.inmem.transport

import com.google.common.eventbus.EventBus
import io.muoncore.channel.ChannelConnection
import io.muoncore.memory.transport.DefaultInMemClientChannelConnection
import io.muoncore.memory.transport.OpenChannelEvent
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.protocol.ChannelFunctionExecShimBecauseGroovyCantCallLambda
import spock.lang.Specification

class InMemClientChannelConnectionSpec extends Specification {

    def "channel sends OpenChannelEvent on receive being called"() {
        given:
        def eventbus = Mock(EventBus)
        def ret = new DefaultInMemClientChannelConnection("simples", "fakeproto", eventbus)

        when:
        ret.receive({})

        then:
        1 * eventbus.post({ OpenChannelEvent ev ->
            ev.clientChannelConnection == ret &&
                    ev.protocol == "fakeproto" &&
                    ev.targetService == "simples"
        })
    }

    def "on send() waits for attaching of remote server before sending to ensure full channel setup with no race"() {
        given:
        def eventbus = Mock(EventBus)
        def serverConnection = Mock(ChannelConnection)
        def ret = new DefaultInMemClientChannelConnection("simples", "fakeproto", eventbus)

        def msg = MuonMessageBuilder.fromService("tombola").build()

        when:
        Thread.start {
            Thread.sleep(100)
            ret.attachServerConnection(serverConnection)
        }

        ret.send(msg)
        ret.send(msg)

        then:
        2 * serverConnection.send({ MuonInboundMessage message ->
            message.id == msg.id
        })
    }

    def "attachServerConnection, registers transformer function to pull in data from remote"() {
        given:
        def function
        def localFunction = Mock(ChannelConnection.ChannelFunction)
        def eventbus = Mock(EventBus)
        def serverConnection = Mock(ChannelConnection) {
            receive(_) >> { args ->
                function = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(args[0])
            }
        }
        def ret = new DefaultInMemClientChannelConnection("simples", "fakeproto", eventbus)
        ret.receive(localFunction)

        when:
        ret.attachServerConnection(serverConnection)
        function(MuonMessageBuilder.fromService("tombola")
                        .build())

        then:
        1 * localFunction.apply({ MuonInboundMessage msg ->
            msg.targetServiceName == "tombola"
        })
    }
}
