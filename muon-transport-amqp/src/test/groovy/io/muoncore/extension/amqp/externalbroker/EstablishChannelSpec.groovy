package io.muoncore.extension.amqp.externalbroker

import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.GsonCodec
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

        AMQPMuonTransport svc1 = createTransport("service1", serverStacks1)
        AMQPMuonTransport svc2 = createTransport("tombola", serverStacks2)

        svc2.start()
        svc1.start()

        when:
        def channel = svc1.openClientChannel("tombola", "requestresponse")

        channel.send(new TransportOutboundMessage(
                "somethingHappened",
                "1",
                "tombola",
                RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                [:],
                "applicaton/json",
                new GsonCodec().encode([:])))
        sleep(50)

        then:
        1 * serverStacks2.openServerChannel("requestresponse") >> Mock(ChannelConnection)
    }

    private AMQPMuonTransport createTransport(serviceName, serverStacks) {

        def connection = new RabbitMq09ClientAmqpConnection("amqp://muon:microservices@localhost")
        def queueFactory = new RabbitMq09QueueListenerFactory(connection.channel)
        def serviceQueue = new DefaultServiceQueue(serviceName, connection)
        def channelFactory = new DefaultAmqpChannelFactory(serviceName, queueFactory, connection)

        def svc1 = new AMQPMuonTransport(
                "amqp://muon:microservices@localhost", serviceName, serverStacks, serviceQueue, channelFactory)
        svc1
    }
}
