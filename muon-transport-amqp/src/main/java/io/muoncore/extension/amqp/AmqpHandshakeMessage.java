package io.muoncore.extension.amqp;

public class AmqpHandshakeMessage {

    private String protocol;
    private String sourceHost;
    private String replyQueue;
    private String receiveQueue;

    public AmqpHandshakeMessage(String protocol, String sourceHost, String replyQueue, String receiveQueue) {
        this.protocol = protocol;
        this.sourceHost = sourceHost;
        this.replyQueue = replyQueue;
        this.receiveQueue = receiveQueue;
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

    public String getReceiveQueue() {
        return receiveQueue;
    }
}
