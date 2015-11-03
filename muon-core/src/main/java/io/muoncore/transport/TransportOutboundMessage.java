package io.muoncore.transport;

import java.util.List;
import java.util.Map;

public class TransportOutboundMessage extends TransportMessage {

    public TransportOutboundMessage(String type,
                                    String id,
                                    String targetServiceName,
                                    String sourceServiceName,
                                    String protocol,
                                    Map<String, String> metadata,
                                    String contentType,
                                    byte[] payload,
                                    List<String> sourceAvailableContentTypes) {
        super(type, id, targetServiceName, sourceServiceName, protocol, metadata, contentType, payload, sourceAvailableContentTypes);
    }

    public TransportOutboundMessage cloneWithProtocol(String protocol) {
        return new TransportOutboundMessage(
                getType(),
                getId(),
                getTargetServiceName(),
                getSourceServiceName(),
                protocol,
                getMetadata(),
                getContentType(),
                getPayload(),
                getSourceAvailableContentTypes());
    }

    public TransportInboundMessage toInbound() {
        return new TransportInboundMessage(
                getType(),
                getId(),
                getTargetServiceName(),
                getSourceServiceName(),
                getProtocol(),
                getMetadata(),
                getContentType(),
                getPayload(),
                getSourceAvailableContentTypes());
    }
}
