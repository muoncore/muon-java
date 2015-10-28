package io.muoncore.memory.transport;

public class OpenChannelEvent {
    private String targetService;
    private String protocol;
    private InMemClientChannelConnection clientChannelConnection;

    public OpenChannelEvent(String targetService, String protocol,
                            InMemClientChannelConnection clientChannelConnection) {
        this.targetService = targetService;
        this.protocol = protocol;
        this.clientChannelConnection = clientChannelConnection;
    }

    public InMemClientChannelConnection getClientChannelConnection() {
        return clientChannelConnection;
    }

    public String getTargetService() {
        return targetService;
    }

    public String getProtocol() {
        return protocol;
    }
}
