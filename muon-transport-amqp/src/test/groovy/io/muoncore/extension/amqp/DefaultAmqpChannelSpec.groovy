package io.muoncore.extension.amqp

import spock.lang.Specification

import static io.muoncore.extension.amqp.QueueListener.*

class DefaultAmqpChannelSpec extends Specification {


    def "respondToHandshake opens a new queue and sends a handshak response"() {
        given:
        def listenerfactory = Mock(QueueListenerFactory)
        def connection = Mock(AmqpConnection)
        def channel = new DefaultAmqpChannel(connection, listenerfactory, "myawesomeservice")
        def localQueue

        when:
        channel.respondToHandshake(new AmqpHandshakeMessage("fakeproto", "remoteservice", "my-reply-queue", "receive-queue"))

        then:
        1 * listenerfactory.listenOnQueue(_, _ as QueueFunction) >> { args -> localQueue = args[0]; return null}
        1 * connection.send({ QueueMessage message ->
            message.queueName == "my-reply-queue" &&
                    message.headers[AMQPMuonTransport.HEADER_PROTOCOL] == "fakeproto"

        } as QueueMessage)
    }

    def "initiateFromHandshake opens a new queue and sends a handshake response"() {
        given:
        def listenerfactory = Mock(QueueListenerFactory)
        def connection = Mock(AmqpConnection)
        def channel = new DefaultAmqpChannel(connection, listenerfactory, "awesomeservice")

        when:
        channel.initiateHandshake("remoteservice", "fakeproto")

        then:
        1 * listenerfactory.listenOnQueue(_, _ as QueueFunction)
        1 * connection.send({ QueueMessage message ->
            message.queueName == "service.remoteservice" &&
                    message.headers[AMQPMuonTransport.HEADER_PROTOCOL] == "fakeproto" &&
                    message.headers[AMQPMuonTransport.HEADER_SOURCE_SERVICE] == "awesomeservice" &&
                    message.headers[AMQPMuonTransport.HEADER_REPLY_TO] != null &&
                    message.headers[AMQPMuonTransport.HEADER_RECEIVE_QUEUE] != null
        })
    }
}
