package io.muoncore.extension.amqp

import io.muoncore.protocol.requestresponse.RRPTransformers
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportOutboundMessage
import spock.lang.Specification

class AmqpMessageTransformersSpec extends Specification {

    def "outboundToQueue"() {
        when:
        def queueMessage = AmqpMessageTransformers.outboundToQueue("myQueue", outbound())

        then:
        queueMessage != null
        queueMessage.queueName == "myQueue"
        queueMessage.contentType == outbound().contentType
        queueMessage.headers == outbound().contentType

    }

    def "queueToInbound"() {

    }

    TransportOutboundMessage outbound() {
        new TransportOutboundMessage(
                "123",
                "myService",
                RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                [:],
                "application/json",
                [] as byte[])
    }
    TransportInboundMessage inbound() {
        new TransportInboundMessage(
                "123",
                "myService",
                RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                [:],
                "application/json",
                [] as byte[]
        )
    }
}
