package io.muoncore.inmem.transport

import com.google.common.eventbus.EventBus
import io.muoncore.channel.ChannelConnection
import io.muoncore.memory.transport.InMemClientChannelConnection
import io.muoncore.memory.transport.InMemServer
import io.muoncore.memory.transport.OpenChannelEvent
import io.muoncore.protocol.ServerStacks
import spock.lang.Specification

class InMemServerSpec extends Specification {

    def "server registers with event bus"() {
        given:
        def eventbus = Mock(EventBus)
        def serverStacks = Mock(ServerStacks)

        when:
        new InMemServer("tombola", eventbus, serverStacks)

        then:
        eventbus.register(_ instanceof InMemServer)
    }

    def "server receives OpenChannelEvents for the defined service, creates server channels and attaches them to the client channel"() {

        given:
        def eventBus = Mock(EventBus)
        def clientChannel = Mock(InMemClientChannelConnection)
        def serverChannel = Mock(ChannelConnection)
        def openChannelEvent = new OpenChannelEvent("tombola", "fakeproto", clientChannel)
        def stacks = Mock(ServerStacks)

        def server = new InMemServer("tombola", eventBus, stacks)

        when:
        server.onOpenChannel(openChannelEvent)

        then:
        1 * stacks.openServerChannel("fakeproto") >> serverChannel
        1 * clientChannel.attachServerConnection(serverChannel)
    }

    def "server only responds to OpenChannelEvents for the defined service"() {

        given:
        def eventBus = Mock(EventBus)
        def clientChannel = Mock(InMemClientChannelConnection)
        def serverChannel = Mock(ChannelConnection)
        def openChannelEvent = new OpenChannelEvent(serverName, "fakeproto", clientChannel)
        def stacks = Mock(ServerStacks)

        def server = new InMemServer("tombola", eventBus, stacks)

        when:
        server.onOpenChannel(openChannelEvent)

        then:
        numInvocations * stacks.openServerChannel("fakeproto") >> serverChannel
        numInvocations * clientChannel.attachServerConnection(serverChannel)

        where:

        serverName || numInvocations

        "tombola"  || 1
        "wibble"   || 0
        "simples"  || 0
        "tombola"  || 1
        "nope"     || 0
    }
}