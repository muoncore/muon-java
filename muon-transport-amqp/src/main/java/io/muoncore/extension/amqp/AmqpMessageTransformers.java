package io.muoncore.extension.amqp;

import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonOutboundMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AmqpMessageTransformers {

    private static Logger LOG = Logger.getLogger(ServiceQueue.class.getCanonicalName());


    public static QueueListener.QueueMessage outboundToQueue(String queue, MuonOutboundMessage message) {

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

        return new QueueListener.QueueMessage(message.getStep(), queue, message.getPayload(), headers, message.getContentType());
    }

    public static MuonInboundMessage queueToInbound(QueueListener.QueueMessage message) {
        Map metadata = new HashMap<>();
        metadata.putAll(message.getHeaders());

        String targetService = "unknown";

        if (!isvalid("id", message)) {
            LOG.log(Level.SEVERE, "Header missing id");
        }
        if (!isvalid("sourceService", message)) {
            LOG.log(Level.SEVERE, "Header missing sourceService");
        }
        if (!isvalid("protocol", message)) {
            LOG.log(Level.SEVERE, "Header missing protocol");
        }
        if (!isvalid("sourceAvailableContentTypes", message)) {
            LOG.log(Level.SEVERE, "Header missing sourceAvailableContentTypes");
        }
        if (!isvalid("channelOperation", message)) {
            LOG.log(Level.SEVERE, "Header missing channelOperation");
        }

        if (message.getHeaders().get("targetService") != null) {
            targetService = message.getHeaders().get("targetService");
        }

        return new MuonInboundMessage(
                message.getEventType(),
                message.getHeaders().get("id").toString(),
                targetService,
                message.getHeaders().get("sourceService").toString(),
                message.getHeaders().get("protocol").toString(),
                metadata,
                message.getContentType(), message.getBody(),
                Arrays.asList(message.getHeaders().get("sourceAvailableContentTypes").split("|")),
                MuonMessage.ChannelOperation.valueOf(message.getHeaders().get("channelOperation")));
    }
    
    static boolean isvalid(String name, QueueListener.QueueMessage message) {
        return message.getHeaders().get(name) != null;
    }
}
