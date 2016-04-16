package io.muoncore.extension.amqp

import io.muoncore.protocol.requestresponse.RRPTransformers
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonOutboundMessage
import spock.lang.Specification

class AmqpMessageTransformersSpec extends Specification {

    def "outboundToQueue"() {
        when:
        def queueMessage = AmqpMessageTransformers.outboundToQueue("myQueue", outbound())

        then:
        queueMessage != null
        queueMessage.queueName == "myQueue"
        queueMessage.contentType == outbound().contentType

    }

    def "queueToInbound"() {

    }

    MuonOutboundMessage outbound() {
        new MuonOutboundMessage(
                "somethingHappened",
                "123",
                "theirService",
                "myService",
                RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                [:],
                "application/json",
                [] as byte[], [])
    }
    MuonInboundMessage inbound() {
        new MuonInboundMessage(
                "somethingHappened",
                "123",
                "theirService",
                "myService",
                RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                [:],
                "application/json",
                [] as byte[], []
        )
    }
}
