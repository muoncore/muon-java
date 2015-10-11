package io.muoncore.transport;

public class TransportInboundMessage {

    private String id;
    private String sourceServiceName;
    private String protocol;

    public TransportInboundMessage(String id, String sourceServiceName, String protocol) {
        this.id = id;
        this.sourceServiceName = sourceServiceName;
        this.protocol = protocol;
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
