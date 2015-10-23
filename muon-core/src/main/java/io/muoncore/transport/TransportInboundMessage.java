package io.muoncore.transport;

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
            byte[] payload) {
        super(type, id, targetServiceName, sourceServiceName, protocol, metadata, contentType, payload);
    }
}
