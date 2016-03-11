package io.muoncore.extension.amqp;

import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AmqpMessageTransformers {

    public static QueueListener.QueueMessage outboundToQueue(String queue, TransportOutboundMessage message) {

        Map<String, String> headers = new HashMap<>();
        headers.putAll(message.getMetadata());
        headers.put("id", message.getId());
        headers.put("sourceService", message.getSourceServiceName());
        headers.put("targetService", message.getTargetServiceName());
        headers.put("protocol", message.getProtocol());

        String delimitedContentTypes = message.getSourceAvailableContentTypes().stream()
                .collect(Collectors.joining("|"));
        headers.put("sourceAvailableContentTypes", delimitedContentTypes);
        headers.put("channelOperation", message.getChannelOperation().toString());

        return new QueueListener.QueueMessage(message.getType(), queue, message.getPayload(), headers, message.getContentType());
    }

    public static TransportInboundMessage queueToInbound(QueueListener.QueueMessage message) {
        Map metadata = new HashMap<>();
        metadata.putAll(message.getHeaders());

        String targetService = "unknown";

        if (message.getHeaders().get("targetService") != null) {
            targetService = message.getHeaders().get("targetService");
        }

        return new TransportInboundMessage(
                message.getEventType(),
                message.getHeaders().get("id").toString(),
                targetService,
                message.getHeaders().get("sourceService").toString(),
                message.getHeaders().get("protocol").toString(),
                metadata,
                message.getContentType(), message.getBody(),
                Arrays.asList(message.getHeaders().get("sourceAvailableContentTypes").split("|")),
                TransportMessage.ChannelOperation.valueOf(message.getHeaders().get("channelOperation")));
    }

}
