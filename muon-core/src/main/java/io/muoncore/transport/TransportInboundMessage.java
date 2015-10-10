package io.muoncore.transport;

public class TransportInboundMessage {

    private String id;
    private String serviceName;
    private String channelName;

    public TransportInboundMessage(String id, String serviceName, String channelName) {
        this.id = id;
        this.serviceName = serviceName;
        this.channelName = channelName;
    }

    public String getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getChannelName() {
        return channelName;
    }
}
