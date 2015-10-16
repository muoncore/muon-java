package io.muoncore.extension.amqp;

import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DefaultAmqpChannel implements AmqpChannel {

    private String sendQueue;
    private String receiveQueue;
    private QueueListenerFactory listenerFactory;
    private ChannelFunction<TransportInboundMessage> function;
    private AmqpConnection connection;

    public DefaultAmqpChannel(AmqpConnection connection, QueueListenerFactory queueListenerFactory) {
        this.connection = connection;
        this.listenerFactory = queueListenerFactory;
    }

    @Override
    public void initiateHandshake(String serviceName, String protocol) {

    }

    @Override
    public void respondToHandshake(AmqpHandshakeMessage message) {
        receiveQueue = UUID.randomUUID().toString();
        sendQueue = message.getReplyQueue();
        listenerFactory.listenOnQueue(receiveQueue, msg -> {
            if (function != null) {
                function.apply(AmqpMessageTransformers.queueToInbound(msg));
            }
        });

        Map<String, Object> headers = new HashMap<>();
        headers.put(AMQPMuonTransport.HEADER_PROTOCOL, message.getProtocol());
        headers.put(AMQPMuonTransport.HEADER_REPLY_TO, receiveQueue);

        try {
            connection.send(new QueueListener.QueueMessage(message.getReplyQueue(), new byte[0], headers, "text/plain"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receive(ChannelFunction<TransportInboundMessage> function) {
        this.function = function;
    }

    @Override
    public void send(TransportOutboundMessage message) {
        try {
            connection.send(AmqpMessageTransformers.outboundToQueue(message));
        } catch (IOException e) {
            //TODO, reply back with an error message?
            e.printStackTrace();
        }
    }
}
