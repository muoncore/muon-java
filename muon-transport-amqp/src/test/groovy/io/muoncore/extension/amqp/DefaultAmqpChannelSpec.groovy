package io.muoncore.extension.amqp

import io.muoncore.Discovery
import io.muoncore.codec.json.JsonOnlyCodecs
import reactor.Environment
import spock.lang.Specification

import static io.muoncore.extension.amqp.QueueListener.QueueFunction
import static io.muoncore.extension.amqp.QueueListener.QueueMessage

class DefaultAmqpChannelSpec extends Specification {

    def discovery = Mock(Discovery)
    def codecs = new JsonOnlyCodecs()

    def "if no message received by server channel, close and send transport error to protocol"() {

    }

    def "if no message received by client channel, close and send transport error to protocol"() {

    }

    def "client sends a keep alive every second if no data"() {

    }

    def "server sends a keep alive every second if no other data"() {

    }

    def "respondToHandshake opens a new queue and sends a handshake response"() {
        given:
        Environment.initializeIfEmpty()
        def listenerfactory = Mock(QueueListenerFactory)
        def connection = Mock(AmqpConnection)
        def channel = new DefaultAmqpChannel(connection, listenerfactory, "myawesomeservice", Environment.sharedDispatcher(), codecs, discovery)
        def localQueue

        when:
        channel.respondToHandshake(new AmqpHandshakeMessage("fakeproto", "my-reply-queue", "receive-queue"))

        then:
        1 * listenerfactory.listenOnQueue(_, _ as QueueFunction) >> { args -> localQueue = args[0]; return null }
        1 * connection.send({ QueueMessage message ->
            message.queueName == "my-reply-queue" &&
                    message.headers[QueueMessageBuilder.HEADER_PROTOCOL] == "fakeproto"

        } as QueueMessage)
    }

    def "initiateFromHandshake opens a new queue and sends a handshake response"() {
        given:
        Environment.initializeIfEmpty()
        def listenerfactory = Mock(QueueListenerFactory)
        def connection = Mock(AmqpConnection)
        def channel = new DefaultAmqpChannel(connection, listenerfactory, "awesomeservice", Environment.sharedDispatcher(), codecs, discovery)

        when:
        Thread.start {
            channel.initiateHandshake("remoteservice", "fakeproto")
        }
        sleep(100)
        then:
        1 * listenerfactory.listenOnQueue(_, _ as QueueFunction)
        1 * connection.send({ QueueMessage message ->
            message.queueName == "service.remoteservice" &&
                    message.headers[QueueMessageBuilder.HEADER_PROTOCOL] == "fakeproto" &&
                    message.headers[QueueMessageBuilder.HEADER_REPLY_TO] != null &&
                    message.headers[QueueMessageBuilder.HEADER_RECEIVE_QUEUE] != null
        } as QueueMessage)
    }
}
