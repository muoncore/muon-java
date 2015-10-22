package io.muoncore.transport;

import java.util.Map;

public class TransportInboundMessage extends TransportMessage {
    public TransportInboundMessage(
            String id,
            String sourceServiceName,
            String protocol,
            Map<String, String> metadata,
            String contentType,
            byte[] payload) {
        super(id, sourceServiceName, protocol, metadata, contentType, payload);
    }
}
