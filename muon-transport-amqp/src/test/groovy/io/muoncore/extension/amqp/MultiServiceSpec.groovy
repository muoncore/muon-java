package io.muoncore.extension.amqp

import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection
import io.muoncore.protocol.ServerStacks
import io.muoncore.transport.TransportOutboundMessage
import spock.lang.Specification

class MultiServiceSpec extends Specification {

    def serverStacks1 = Mock(ServerStacks)
    def serverStacks2 = Mock(ServerStacks)

    def "two transports can establish an AMQP channel between them"() {

        AMQPMuonTransport svc1 = createTransport("service1", serverStacks1)
        AMQPMuonTransport svc2 = createTransport("tombola", serverStacks2)

        svc1.start()
        svc2.start()

        sleep(1000)

        when:
        def channel = svc1.openClientChannel("tombola", "requestresponse")

        channel.send(new TransportOutboundMessage("1", ))
        sleep(50)

        then:
        1 * serverStacks2.openServerChannel("requestresponse")
    }



    private AMQPMuonTransport createTransport(serviceName, serverStacks) {

        def connection = new RabbitMq09ClientAmqpConnection("amqp://muon:microservices@localhost")
        def serviceQueue = new DefaultServiceQueue(serviceName, connection)
        def channelFactory = new DefaultAmqpChannelFactory()

        def svc1 = new AMQPMuonTransport(
                "amqp://muon:microservices@localhost", serviceName, serverStacks, serviceQueue, channelFactory)
        svc1
    }
}
