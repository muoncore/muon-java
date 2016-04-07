package io.muoncore.extension.amqp

import reactor.Environment
import spock.lang.Specification

import static io.muoncore.extension.amqp.QueueListener.QueueFunction
import static io.muoncore.extension.amqp.QueueListener.QueueMessage

class DefaultAmqpChannelSpec extends Specification {

    def "respondToHandshake opens a new queue and sends a handshake response"() {
        given:
        Environment.initializeIfEmpty()
        def listenerfactory = Mock(QueueListenerFactory)
        def connection = Mock(AmqpConnection)
        def channel = new DefaultAmqpChannel(connection, listenerfactory, "myawesomeservice", Environment.sharedDispatcher())
        def localQueue

        when:
        channel.respondToHandshake(new AmqpHandshakeMessage("fakeproto", "remoteservice", "my-reply-queue", "receive-queue"))

        then:
        1 * listenerfactory.listenOnQueue(_, _ as QueueFunction) >> { args -> localQueue = args[0]; return null }
        1 * connection.send({ QueueMessage message ->
            message.queueName == "my-reply-queue" &&
                    message.headers[AMQPMuonTransport.HEADER_PROTOCOL] == "fakeproto"

        } as QueueMessage)
    }

    def "initiateFromHandshake opens a new queue and sends a handshake response"() {
        given:
        Environment.initializeIfEmpty()
        def listenerfactory = Mock(QueueListenerFactory)
        def connection = Mock(AmqpConnection)
        def channel = new DefaultAmqpChannel(connection, listenerfactory, "awesomeservice", Environment.sharedDispatcher())

        when:
        Thread.start {
            channel.initiateHandshake("remoteservice", "fakeproto")
        }
        sleep(100)
        then:
        1 * listenerfactory.listenOnQueue(_, _ as QueueFunction)
        1 * connection.send({ QueueMessage message ->
            message.queueName == "service.remoteservice" &&
                    message.headers[AMQPMuonTransport.HEADER_PROTOCOL] == "fakeproto" &&
                    message.headers[AMQPMuonTransport.HEADER_SOURCE_SERVICE] == "awesomeservice" &&
                    message.headers[AMQPMuonTransport.HEADER_REPLY_TO] != null &&
                    message.headers[AMQPMuonTransport.HEADER_RECEIVE_QUEUE] != null
        } as QueueMessage)
    }
}
