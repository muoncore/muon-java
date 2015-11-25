package io.muoncore.extension.amqp

import io.muoncore.channel.ChannelConnection
import io.muoncore.protocol.ServerStacks
import reactor.Environment
import spock.lang.Specification

class AMQPMuonTransportSpec extends Specification {

    def "Opens listen service queue for handshake. For every handshake, create a new channel"() {
        Environment.initializeIfEmpty()

        def serverStacks = Mock(ServerStacks)
        def serviceQueue = Mock(ServiceQueue)
        def channelFactory = Mock(AmqpChannelFactory)
        def transport = new AMQPMuonTransport(
            "url", serviceQueue, channelFactory
        )

        when:
        transport.start(serverStacks)

        then:
        1 * serviceQueue.onHandshake(_)
    }


    def "handshake processing causes a channel to be created"() {
        Environment.initializeIfEmpty()

        def mockChannel = Mock(AmqpChannel)
        def mockServerChannelConnection = Mock(ChannelConnection)
        def func
        def serverStacks = Mock(ServerStacks)
        def serviceQueue = Mock(ServiceQueue) {
            onHandshake(_) >> { args ->
                    func = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(args[0])
            }
        }
        def channelFactory = Mock(AmqpChannelFactory)
        def transport = new AMQPMuonTransport(
                "url", serviceQueue, channelFactory
        )
        Thread.sleep(50)

        when:
        transport.start(serverStacks)
        func(new AmqpHandshakeMessage("myfakeproto", "", "", ""))

        then:
        1 * channelFactory.createChannel() >> mockChannel
        1 * mockChannel.respondToHandshake(_ as AmqpHandshakeMessage)
        1 * serverStacks.openServerChannel("myfakeproto") >> mockServerChannelConnection
    }

    def "open channel will cause a channel to be created with handshake initiated"() {
        Environment.initializeIfEmpty()

        def mockChannel = Mock(AmqpChannel)
        def serverStacks = Mock(ServerStacks)
        def serviceQueue = Mock(ServiceQueue)
        def channelFactory = Mock(AmqpChannelFactory)
        def transport = new AMQPMuonTransport(
                "url", serviceQueue, channelFactory
        )
        Thread.sleep(50)

        when:
        transport.start(serverStacks)
        transport.openClientChannel("someRemoteService", "fakeproto")

        then:
        1 * channelFactory.createChannel() >> mockChannel
        1 * mockChannel.initiateHandshake("someRemoteService", "fakeproto")
    }
}
