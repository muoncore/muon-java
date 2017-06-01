package io.muoncore.extension.amqp

import io.muoncore.Discovery
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.message.MuonMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonOutboundMessage
import spock.lang.Specification

class AmqpMessageTransformersSpec extends Specification {

    def discovery = Mock(Discovery)
    def codecs = new JsonOnlyCodecs()

    def "outboundToQueue"() {
        when:
        def queueMessage = AmqpMessageTransformers.outboundToQueue("myQueue", outbound(), codecs, discovery)

        then:
        queueMessage != null
        queueMessage.queueName == "myQueue"
        queueMessage.contentType == outbound().contentType

    }

    MuonOutboundMessage outbound() {
        MuonMessageBuilder
                .fromService("myService")
                .step("somethingHappened")
                .protocol(RRPTransformers.REQUEST_RESPONSE_PROTOCOL)
                .toService("theirService")
                .payload([] as byte[])
                .contentType("application/json")
                .status(MuonMessage.Status.success)
                .build()

    }
    MuonInboundMessage inbound() {
        MuonMessageBuilder
                .fromService("myService")
                .step("somethingHappened")
                .protocol(RRPTransformers.REQUEST_RESPONSE_PROTOCOL)
                .toService("theirService")
                .payload([] as byte[])
                .contentType("application/json")
                .status(MuonMessage.Status.success)
                .buildInbound()

    }
}
