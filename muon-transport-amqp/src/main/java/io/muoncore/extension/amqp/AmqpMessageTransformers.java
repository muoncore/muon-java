package io.muoncore.extension.amqp;

import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.HashMap;
import java.util.Map;

public class AmqpMessageTransformers {


//    these are utterly broken and need to be fixed for the greater good.


    public static QueueListener.QueueMessage outboundToQueue(String queue, TransportOutboundMessage message) {

        Map<String, String> headers = new HashMap<>();
        headers.put("id", message.getId());
        headers.put("sourceService", message.getSourceServiceName());
        headers.put("targetService", message.getTargetServiceName());
        headers.put("protocol", message.getProtocol());
        headers.putAll(message.getMetadata());

        return new QueueListener.QueueMessage(message.getType(), queue, message.getPayload(), headers, message.getContentType());
    }

    public static TransportInboundMessage queueToInbound(QueueListener.QueueMessage message) {
        Map metadata = new HashMap<>();
        metadata.putAll(message.getHeaders());

        return new TransportInboundMessage(
                message.getEventType(),
                message.getHeaders().get("id").toString(),
                message.getHeaders().get("targetService").toString(),
                message.getHeaders().get("sourceService").toString(),
                message.getHeaders().get("protocol").toString(),
                metadata,
                message.getContentType(), message.getBody());
    }

}
