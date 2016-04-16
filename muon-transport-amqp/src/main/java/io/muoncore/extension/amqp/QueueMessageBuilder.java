package io.muoncore.extension.amqp;

import java.util.HashMap;
import java.util.Map;

public class QueueMessageBuilder {

    public final static String HEADER_PROTOCOL = "protocol";
    public final static String HEADER_REPLY_TO = "server_reply_q";
    public final static String HEADER_RECEIVE_QUEUE = "server_listen_q";
    public final static String HEADER_CONTENT_TYPE = "content_type";
    public final static String HEADER_HANDSHAKE = "handshake";

    private String queueName;
    private Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public static QueueMessageBuilder queue(String queueName) {
        QueueMessageBuilder b = new QueueMessageBuilder();
        b.queueName = queueName;
        return b;
    }

    public QueueMessageBuilder body(byte[] body) {
        this.body = body;
        return this;
    }

    public QueueMessageBuilder protocol(String protocol) {
        headers.put(HEADER_PROTOCOL, protocol);
        return this;
    }

    public QueueMessageBuilder serverReplyTo(String sendQueue) {
        headers.put(HEADER_REPLY_TO, sendQueue);
        return this;
    }

    public QueueMessageBuilder recieveQueue(String queueName) {
        headers.put(HEADER_RECEIVE_QUEUE, queueName);
        return this;
    }

    public QueueMessageBuilder contentType(String contentType) {
        headers.put(HEADER_CONTENT_TYPE, contentType);
        return this;
    }

    public QueueMessageBuilder handshakeMessage(String message) {
        headers.put(HEADER_HANDSHAKE, message);
        return this;
    }

    public QueueListener.QueueMessage build() {
        return new QueueListener.QueueMessage(queueName, body, headers);
    }
}
