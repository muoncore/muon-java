package io.muoncore.extension.amqp;

import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

public class AmqpMessageTransformers {

    public static QueueListener.QueueMessage outboundToQueue(TransportOutboundMessage message) {
        return null;
    }

    public static TransportInboundMessage queueToInbound(QueueListener.QueueMessage message) {
        return null;
    }

}
