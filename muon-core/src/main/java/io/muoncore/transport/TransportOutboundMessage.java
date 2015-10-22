package io.muoncore.transport;

import java.util.Map;

public class TransportOutboundMessage extends TransportMessage {

    private boolean closeChannel = false;

    public TransportOutboundMessage(String id,
                                    String sourceServiceName,
                                    String protocol,
                                    Map<String, String> metadata,
                                    String contentType,
                                    byte[] payload) {
        super(id, sourceServiceName, protocol, metadata, contentType, payload);
    }

    public TransportOutboundMessage(String id,
                                    String sourceServiceName,
                                    String protocol,
                                    Map<String, String> metadata,
                                    String contentType,
                                    byte[] payload,
                                    boolean closeChannel) {
        super(id, sourceServiceName, protocol, metadata, contentType, payload);
        this.closeChannel = closeChannel;
    }

    public TransportOutboundMessage cloneWithProtocol(String protocol) {
        return new TransportOutboundMessage(getId(),
                getSourceServiceName(),
                protocol,
                getMetadata(),
                getContentType(),
                getPayload(),
                closeChannel);
    }
}
