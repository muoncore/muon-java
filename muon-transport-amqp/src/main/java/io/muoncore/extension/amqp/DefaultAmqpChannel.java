package io.muoncore.extension.amqp;

import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultAmqpChannel implements AmqpChannel {

    private String sendQueue;
    private String receiveQueue;
    private QueueListenerFactory listenerFactory;
    private ChannelFunction<TransportInboundMessage> function;
    private AmqpConnection connection;
    private String localServiceName;
    private Logger log = Logger.getLogger(DefaultAmqpChannel.class.getName());

    private CountDownLatch handshakeControl = new CountDownLatch(1);

    private QueueListener listener;

    public DefaultAmqpChannel(AmqpConnection connection,
                              QueueListenerFactory queueListenerFactory,
                              String localServiceName) {
        this.connection = connection;
        this.listenerFactory = queueListenerFactory;
        this.localServiceName = localServiceName;
    }

    @Override
    public void initiateHandshake(String serviceName, String protocol) {
        receiveQueue = localServiceName + "-receive-" + UUID.randomUUID().toString();
        sendQueue = serviceName + "-receive-" + UUID.randomUUID().toString();

        listener = listenerFactory.listenOnQueue(receiveQueue, msg -> {
            log.log(Level.FINE, "Received a message on the receive queue " + msg.getQueueName() + " of type " + msg.getEventType());
            if (msg.getEventType().equals("handshakeAccepted")) {
                log.log(Level.FINER, "Handshake completed");
                handshakeControl.countDown();
                return;
            }
            if(function != null) {
                function.apply(AmqpMessageTransformers.queueToInbound(msg));
            }
        });

        Map<String, String> headers = new HashMap<>();
        headers.put(AMQPMuonTransport.HEADER_PROTOCOL, protocol);
        headers.put(AMQPMuonTransport.HEADER_REPLY_TO, receiveQueue);
        headers.put(AMQPMuonTransport.HEADER_RECEIVE_QUEUE, sendQueue);
        headers.put(AMQPMuonTransport.HEADER_SOURCE_SERVICE, localServiceName);

        try {
            connection.send(new QueueListener.QueueMessage("handshakeInitiated", "service." + serviceName, new byte[0], headers, "text/plain"));
        } catch (IOException e) {
            throw new MuonTransportFailureException("Unable to initiate handshake", e);
        }
    }

    @Override
    public void respondToHandshake(AmqpHandshakeMessage message) {
        log.log(Level.FINER, "Handshake received " + message.getProtocol());
        receiveQueue = message.getReceiveQueue();
        sendQueue = message.getReplyQueue();
        log.log(Level.FINER, "Opening queue to listen " + receiveQueue);
        listener = listenerFactory.listenOnQueue(receiveQueue, msg -> {
            log.log(Level.FINER, "Received inbound channel message of type " + message.getProtocol());
            if (function != null) {
                function.apply(AmqpMessageTransformers.queueToInbound(msg));
            }
        });

        Map<String, String> headers = new HashMap<>();
        headers.put(AMQPMuonTransport.HEADER_PROTOCOL, message.getProtocol());

        try {
            connection.send(new QueueListener.QueueMessage("handshakeAccepted", message.getReplyQueue(), new byte[0], headers, "text/plain"));
        } catch (IOException e) {
            throw new MuonTransportFailureException("Unable to respond to handshake", e);
        }
    }

    @Override
    public void shutdown() {
        try { listener.cancel(); } catch(Exception e){}
        try { connection.close(); } catch(Exception e){}
    }

    @Override
    public void receive(ChannelFunction<TransportInboundMessage> function) {
        this.function = function;
    }

    @Override
    public void send(TransportOutboundMessage message) {
        try {
            handshakeControl.await(100, TimeUnit.MILLISECONDS);
            log.log(Level.FINER, "Sending inbound channel message of type " + message.getProtocol() + "||" + message.getType());
            connection.send(AmqpMessageTransformers.outboundToQueue(sendQueue, message));
        } catch (IOException | InterruptedException e) {
            throw new MuonTransportFailureException("Did not create a channel within the timeout", e);
        }
    }
}
