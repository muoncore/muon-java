package io.muoncore.extension.amqp;

import io.muoncore.Discovery;
import io.muoncore.channel.async.StandardAsyncChannel;
import io.muoncore.codec.Codecs;
import io.muoncore.exception.MuonException;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import reactor.core.Dispatcher;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultAmqpChannel implements AmqpChannel {

    public static final String CHANNEL_SHUTDOWN = "ChannelShutdown";
    private String sendQueue;
    private String receiveQueue;
    private QueueListenerFactory listenerFactory;
    private ChannelFunction<MuonInboundMessage> function;
    private AmqpConnection connection;
    private String localServiceName;
    private Logger log = Logger.getLogger(DefaultAmqpChannel.class.getName());
    private Dispatcher dispatcher;
    private Codecs codecs;
    private Discovery discovery;

    private CountDownLatch handshakeControl = new CountDownLatch(1);

    private QueueListener listener;

    private ChannelFunction onShutdown;

    private boolean ownsQueues = false;

    public DefaultAmqpChannel(AmqpConnection connection,
                              QueueListenerFactory queueListenerFactory,
                              String localServiceName, Dispatcher dispatcher, Codecs codecs, Discovery discovery) {
        this.connection = connection;
        this.listenerFactory = queueListenerFactory;
        this.localServiceName = localServiceName;
        this.dispatcher = dispatcher;
        this.codecs = codecs;
        this.discovery = discovery;
    }

    @Override
    public void onShutdown(ChannelFunction runnable) {
        this.onShutdown = runnable;
    }

    @Override
    public void initiateHandshake(String serviceName, String protocol) {
        ownsQueues = true;
        receiveQueue = localServiceName + "-receive-" + UUID.randomUUID().toString();
        sendQueue = serviceName + "-receive-" + UUID.randomUUID().toString();

        listener = listenerFactory.listenOnQueue(receiveQueue, msg -> {
            log.log(Level.FINE, "Received a message on the receive queue " + msg.getQueueName());
            if ("accepted".equals(msg.getHandshakeMessage())) {
                log.log(Level.FINE, "Handshake completed");
                handshakeControl.countDown();
                return;
            }
            if (function != null) {
                dispatcher.dispatch(AmqpMessageTransformers.queueToInbound(msg, codecs),
                        function::apply, Throwable::printStackTrace);
            }
        });

        try {
            connection.send(QueueMessageBuilder.queue("service." + serviceName)
                    .protocol(protocol)
                    .serverReplyTo(receiveQueue)
                    .handshakeMessage("initiated")
                    .recieveQueue(sendQueue)
                    .build());
        } catch (IOException e) {
            throw new MuonTransportFailureException("Unable to initiate handshake", e);
        }
        try {
            if (!handshakeControl.await(3000, TimeUnit.MILLISECONDS))
                throw new MuonException("The handshake took too long! target " + serviceName + " / " + protocol + " || " + receiveQueue);
        } catch (InterruptedException e) {
            throw new MuonException("The handshake took too long!", e);
        }
    }

    @Override
    public void respondToHandshake(AmqpHandshakeMessage message) {
        ownsQueues = false;
        log.log(Level.FINE, "Handshake received " + message.getProtocol());
        receiveQueue = message.getReceiveQueue();
        sendQueue = message.getReplyQueue();
        log.log(Level.FINER, "Opening queue to listen " + receiveQueue);
        listener = listenerFactory.listenOnQueue(receiveQueue, msg -> {
            log.log(Level.FINER, "Received inbound channel message of type " + message.getProtocol());
            MuonInboundMessage inboundMessage = AmqpMessageTransformers.queueToInbound(msg, codecs);
            if (StandardAsyncChannel.echoOut) System.out.println(new Date() + ": Channel[ AMQP Wire >>>>> DefaultAMQPChannel]: Received " + inboundMessage);
            if (inboundMessage.getChannelOperation() == MuonMessage.ChannelOperation.closed) {
                function.apply(null);
            } else if (function != null) {
                function.apply(inboundMessage);
            }
        });

        try {
            connection.send(QueueMessageBuilder.queue(message.getReplyQueue())
                    .protocol(message.getProtocol())
                    .handshakeMessage("accepted").build());

        } catch (IOException e) {
            throw new MuonTransportFailureException("Unable to respond to handshake", e);
        } finally {
            handshakeControl.countDown();
        }
    }

    @Override
    public void shutdown() {

        try {
            listener.cancel();
        } catch (Exception e) {
        }
        if (ownsQueues) {
            connection.deleteQueue(sendQueue);
            connection.deleteQueue(receiveQueue);
        }
        if (onShutdown != null) {
            this.onShutdown.apply(null);
        }

    }

    @Override
    public void receive(ChannelFunction<MuonInboundMessage> function) {
        this.function = function;
    }

    @Override
    public void send(MuonOutboundMessage message) {
        if (StandardAsyncChannel.echoOut) System.out.println(new Date() + ": Channel[ DefaultAMQPChannel >>>>> AMQP Wire]: Sending " + message);
        if (message != null) {
            log.log(Level.FINER, "Sending inbound channel message of type " + message.getProtocol() + "||" + message.getStep());
            dispatcher.dispatch(message, msg -> {
                try {
                    connection.send(AmqpMessageTransformers.outboundToQueue(sendQueue, message, codecs, discovery));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, Throwable::printStackTrace);
        }
        if (message == null) {
                send(
                        MuonMessageBuilder.fromService(localServiceName)
                                .step(CHANNEL_SHUTDOWN)
                                .operation(MuonMessage.ChannelOperation.closed)
                                .contentType("text/plain")
                                .payload(new byte[0])
                                .build());
        }
        shutdown();
    }
}
