package io.muoncore.extension.amqp.externalbroker

import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.json.GsonCodec
import io.muoncore.extension.amqp.AMQPMuonTransport
import io.muoncore.extension.amqp.DefaultAmqpChannelFactory
import io.muoncore.extension.amqp.DefaultServiceQueue
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory
import io.muoncore.protocol.ServerStacks
import io.muoncore.protocol.requestresponse.RRPTransformers
import io.muoncore.transport.TransportOutboundMessage
import spock.lang.Specification

class EstablishChannelSpec extends Specification {

    def serverStacks1 = Mock(ServerStacks)
    def serverStacks2 = Mock(ServerStacks)

    def "two transports can establish an AMQP channel between them"() {

        AMQPMuonTransport svc1 = createTransport("service1")
        AMQPMuonTransport svc2 = createTransport("tombola")

        svc2.start(serverStacks2)
        svc1.start(serverStacks1)

        when:
        def channel = svc1.openClientChannel("tombola", "requestresponse")

        channel.send(new TransportOutboundMessage(
                "somethingHappened",
                "1",
                "remoteService",
                "tombola",
                RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                [:],
                "applicaton/json",
                new GsonCodec().encode([:])))
        sleep(50)

        then:
        1 * serverStacks2.openServerChannel("requestresponse") >> Mock(ChannelConnection)

        cleanup:
        svc1.shutdown()
        svc2.shutdown()
    }

    private AMQPMuonTransport createTransport(serviceName) {

        def connection = new RabbitMq09ClientAmqpConnection("amqp://muon:microservices@localhost")
        def queueFactory = new RabbitMq09QueueListenerFactory(connection.channel)
        def serviceQueue = new DefaultServiceQueue(serviceName, connection)
        def channelFactory = new DefaultAmqpChannelFactory(serviceName, queueFactory, connection)

        def svc1 = new AMQPMuonTransport(
                "amqp://muon:microservices@localhost", serviceQueue, channelFactory)
        svc1
    }
}
