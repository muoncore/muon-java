package io.muoncore.extension.amqp;

public class AmqpHandshakeMessage {

    private String protocol;
    private String replyQueue;
    private String receiveQueue;

    public AmqpHandshakeMessage(String protocol, String replyQueue, String receiveQueue) {
        this.protocol = protocol;
        this.replyQueue = replyQueue;
        this.receiveQueue = receiveQueue;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getReplyQueue() {
        return replyQueue;
    }

    public String getReceiveQueue() {
        return receiveQueue;
    }

    @Override
    public String toString() {
        return "AmqpHandshakeMessage{" +
                "protocol='" + protocol + '\'' +
                ", replyQueue='" + replyQueue + '\'' +
                ", receiveQueue='" + receiveQueue + '\'' +
                '}';
    }
}
