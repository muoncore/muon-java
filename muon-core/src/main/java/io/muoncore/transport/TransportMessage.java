package io.muoncore.transport;

import java.util.Map;

public class TransportMessage {
    private String eventType;
    private String id;
    private String sourceServiceName;
    private String protocol;
    private Map<String, String> metadata;
    private byte[] payload;
    private String contentType;

    public TransportMessage(String type,
                            String id,
                            String sourceServiceName,
                            String protocol,
                            Map<String, String> metadata,
                            String contentType,
                            byte[] payload) {
        this.eventType = type;
        this.id = id;
        this.sourceServiceName = sourceServiceName;
        this.protocol = protocol;
        this.metadata = metadata;
        this.payload = payload;
        this.contentType = contentType;
    }

    public String getType() {
        return eventType;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getPayload() {
        return payload;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String getId() {
        return id;
    }

    public String getSourceServiceName() {
        return sourceServiceName;
    }

    public String getProtocol() {
        return protocol;
    }
}
