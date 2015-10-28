package io.muoncore.transport;

import java.util.Map;

public class TransportOutboundMessage extends TransportMessage {

    private boolean closeChannel = false;

    public TransportOutboundMessage(String type,
                                    String id,
                                    String targetServiceName,
                                    String sourceServiceName,
                                    String protocol,
                                    Map<String, String> metadata,
                                    String contentType,
                                    byte[] payload) {
        super(type, id, targetServiceName, sourceServiceName, protocol, metadata, contentType, payload);
    }

    public TransportOutboundMessage(String type,
                                    String id,
                                    String targetServiceName,
                                    String sourceServiceName,
                                    String protocol,
                                    Map<String, String> metadata,
                                    String contentType,
                                    byte[] payload,
                                    boolean closeChannel) {
        super(type, id, targetServiceName, sourceServiceName, protocol, metadata, contentType, payload);
        this.closeChannel = closeChannel;
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
                closeChannel);
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
                getPayload());
    }
}
