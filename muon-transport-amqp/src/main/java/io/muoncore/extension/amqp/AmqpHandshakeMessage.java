package io.muoncore.extension.amqp;

public class AmqpHandshakeMessage {

    private String protocol;
    private String sourceHost;
    private String replyQueue;

    public AmqpHandshakeMessage(String protocol, String sourceHost, String replyQueue) {
        this.protocol = protocol;
        this.sourceHost = sourceHost;
        this.replyQueue = replyQueue;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public String getReplyQueue() {
        return replyQueue;
    }
}
