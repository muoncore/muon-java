package io.muoncore.extension.amqp

import io.muoncore.Discovery
import io.muoncore.ServiceDescriptor
import io.muoncore.channel.ChannelConnection
import io.muoncore.channel.support.Scheduler
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.exception.NoSuchServiceException
import io.muoncore.protocol.ServerStacks
import reactor.Environment
import spock.lang.Specification

class AMQPMuonTransportSpec extends Specification {

    def codecs = new JsonOnlyCodecs()
    def discovery = Mock(Discovery) {
        findService(_) >> Optional.of(new ServiceDescriptor("identifier", [], [], [], []))
    }

    def "Opens listen service queue for handshake. For every handshake, create a new channel"() {
        Environment.initializeIfEmpty()

        def serverStacks = Mock(ServerStacks)
        def serviceQueue = Mock(ServiceQueue)
        def channelFactory = Mock(AmqpChannelFactory)
        def transport = new AMQPMuonTransport(
            "url", serviceQueue, channelFactory
        )

        when:
        transport.start(discovery, serverStacks, codecs, new Scheduler())

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
        transport.start(discovery, serverStacks, codecs, new Scheduler())
        func(new AmqpHandshakeMessage("myfakeproto", "", ""))

        then:
        1 * channelFactory.createChannel() >> mockChannel
        1 * mockChannel.respondToHandshake(_ as AmqpHandshakeMessage)
        1 * serverStacks.openServerChannel("myfakeproto") >> mockServerChannelConnection
        transport.numberOfActiveChannels == 1
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
        transport.start(discovery, serverStacks, codecs, new Scheduler())
        transport.openClientChannel("someRemoteService", "fakeproto")

        then:
        1 * channelFactory.createChannel() >> mockChannel
        1 * mockChannel.initiateHandshake("someRemoteService", "fakeproto")
    }

    def "multi open channel will cause multiple channels to be created with handshake initiated"() {
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
        transport.start(discovery, serverStacks, codecs, new Scheduler())
        transport.openClientChannel("someRemoteService", "fakeproto")
        transport.openClientChannel("someRemoteService2", "fakeproto")

        then:
        2 * channelFactory.createChannel() >> mockChannel
        1 * mockChannel.initiateHandshake("someRemoteService", "fakeproto")
        1 * mockChannel.initiateHandshake("someRemoteService2", "fakeproto")
        transport.numberOfActiveChannels == 2
    }

    def "channel shutdown will cause channel count to reduce and queue to shut down"() {
        Environment.initializeIfEmpty()

        def theShutdown

        def mockChannel = Mock(AmqpChannel) {
            onShutdown(_) >> { args ->
                theShutdown = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(args[0])
            }
        }

        def serverStacks = Mock(ServerStacks)
        def serviceQueue = Mock(ServiceQueue)
        def channelFactory = Mock(AmqpChannelFactory) {
            createChannel() >> mockChannel
        }
        def transport = new AMQPMuonTransport(
                "url", serviceQueue, channelFactory
        )
        Thread.sleep(50)

        when:
        transport.start(discovery, serverStacks, codecs, new Scheduler())
        def c = transport.openClientChannel("someRemoteService2", "fakeproto")

        and:
        theShutdown()

        then:
        transport.numberOfActiveChannels == 0
    }

    def "openClientChannel on non existent service will cause a failure"() {
        given:
        discovery = Mock(Discovery) {
            findService(_) >> Optional.empty()
        }

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

        def channelFactory = Mock(AmqpChannelFactory) {
            createChannel() >> mockChannel
        }

        def transport = new AMQPMuonTransport(
                "url", serviceQueue, channelFactory
        )
        Thread.sleep(50)

        when:
        transport.start(discovery, serverStacks, codecs, new Scheduler())
        def cl = transport.openClientChannel("fakeclient", "myprotoofdoom")

        func(new AmqpHandshakeMessage("myfakeproto", "", ""))

        then: "Some error is thrown. but we don't know what yet..."
        thrown(NoSuchServiceException)
        0 * serverStacks._
    }
}
