package io.muoncore.transport;

public class TransportOutboundMessage {

    private String id;
    private String serviceName;
    private String protocol;

    public TransportOutboundMessage(String id, String serviceName, String protocol) {
        this.id = id;
        this.serviceName = serviceName;
        this.protocol = protocol;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getId() {
        return id;
    }
}
