package io.muoncore.extension.amqp;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListener;

import static io.muoncore.extension.amqp.QueueMessageBuilder.HEADER_PROTOCOL;
import static io.muoncore.extension.amqp.QueueMessageBuilder.HEADER_RECEIVE_QUEUE;
import static io.muoncore.extension.amqp.QueueMessageBuilder.HEADER_REPLY_TO;

public class DefaultServiceQueue implements ServiceQueue {

    private RabbitMq09QueueListener listener;
    private String serviceName;
    private AmqpConnection connection;

    public DefaultServiceQueue(String serviceName, AmqpConnection connection) {
        this.serviceName = serviceName;
        this.connection = connection;
    }

    @Override
    public void shutdown() {
        if (listener != null) listener.cancel();
        try { connection.close(); } catch (Exception ignored){}
    }

    @Override
    public void onHandshake(ChannelConnection.ChannelFunction<AmqpHandshakeMessage> channelFunction) {
        if (listener != null) throw new IllegalStateException("QueueListener already has a handshake.");

        listener = new RabbitMq09QueueListener(connection.getChannel(), "service." + serviceName, fun -> {

            AmqpHandshakeMessage handshake = new AmqpHandshakeMessage(
                    fun.getHeaders().get(HEADER_PROTOCOL).toString(),
                    fun.getHeaders().get(HEADER_REPLY_TO).toString(),
                    fun.getHeaders().get(HEADER_RECEIVE_QUEUE).toString());

            channelFunction.apply(handshake);
        });
        listener.start();
        listener.blockUntilReady();
    }
}
