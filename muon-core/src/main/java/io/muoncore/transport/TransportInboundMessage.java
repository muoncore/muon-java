package io.muoncore.transport;

import java.util.List;
import java.util.Map;

public class TransportInboundMessage extends TransportMessage {

    public TransportInboundMessage(
            String type,
            String id,
            String targetServiceName,
            String sourceServiceName,
            String protocol,
            Map<String, String> metadata,
            String contentType,
            byte[] payload,
            List<String> sourceAvailableContentTypes,
            ChannelOperation channelOperation) {
        super(type, id, targetServiceName, sourceServiceName, protocol, metadata, contentType, payload, sourceAvailableContentTypes, channelOperation);
    }

    public static TransportInboundMessage serviceNotFound(TransportOutboundMessage msg) {
        return new TransportInboundMessage(
                TransportEvents.SERVICE_NOT_FOUND,
                msg.getId(),
                msg.getSourceServiceName(),
                msg.getTargetServiceName(),
                msg.getProtocol(),
                msg.getMetadata(),
                msg.getContentType(),
                msg.getPayload(),
                msg.getSourceAvailableContentTypes(),
                ChannelOperation.NORMAL);
    }
}
