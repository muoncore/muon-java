package io.muoncore.transport;

public class TransportOutboundMessage {

    private String id;
    private String serviceName;
    private String protocol;
    private boolean closeChannel = false;

    public TransportOutboundMessage(String id, String serviceName, String protocol) {
        this.id = id;
        this.serviceName = serviceName;
        this.protocol = protocol;
    }

    public TransportOutboundMessage(String id, String serviceName, String protocol, boolean closeChannel) {
        this.id = id;
        this.serviceName = serviceName;
        this.protocol = protocol;
        this.closeChannel = closeChannel;
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
