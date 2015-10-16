package io.muoncore.extension.amqp

import spock.lang.Specification

import static io.muoncore.extension.amqp.QueueListener.*

class AmqpChannelSpec extends Specification {


    def "respondToHandshake opens a new queue and sends a handshak response"() {
        given:
        def listenerfactory = Mock(QueueListenerFactory)
        def connection = Mock(AmqpConnection)
        def channel = new DefaultAmqpChannel(connection, listenerfactory)
        def localQueue

        when:
        channel.respondToHandshake(new AmqpHandshakeMessage("fakeproto", "remoteservice", "my-reply-queue"))

        then:
        1 * listenerfactory.listenOnQueue(_, _ as QueueFunction) >> { args -> localQueue = args[0]; return null}
        1 * connection.send({ QueueMessage message ->
            message.queueName == "my-reply-queue" &&
                    message.headers[AMQPMuonTransport.HEADER_PROTOCOL] == "fakeproto" &&
                    message.headers[AMQPMuonTransport.HEADER_REPLY_TO] == localQueue

        } as QueueMessage)
    }

    def "initiateFromHandshake opens a new queue and sends a handshake response"() {
        given:
        def listenerfactory = Mock(QueueListenerFactory)
        def connection = Mock(AmqpConnection)
        def channel = new DefaultAmqpChannel(connection, listenerfactory)

        when:
        channel.initiateHandshake("remoteservice", "fakeproto")

        then:
        1 * listenerfactory.listenOnQueue(_, _ as QueueFunction)
    }

    def "Channel "() {



        expect:
        throw new IllegalStateException("Not tested")
    }
}
