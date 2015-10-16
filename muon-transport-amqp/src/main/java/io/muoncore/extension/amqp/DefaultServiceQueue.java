package io.muoncore.extension.amqp;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListener;

public class DefaultServiceQueue implements ServiceQueue {

    private RabbitMq09QueueListener listener;
    private String serviceName;
    private AmqpConnection connection;

    public DefaultServiceQueue(String serviceName, AmqpConnection connection) {
        this.serviceName = serviceName;
        this.connection = connection;
    }

    @Override
    public void onHandshake(ChannelConnection.ChannelFunction<AmqpHandshakeMessage> channelFunction) {
        if (listener != null) throw new IllegalStateException("QueueListener already has a handshake.");

        listener = new RabbitMq09QueueListener(connection.getChannel(), "service." + serviceName, fun -> {
            AmqpHandshakeMessage handshake = new AmqpHandshakeMessage(
                    fun.getHeaders().get(AMQPMuonTransport.HEADER_PROTOCOL).toString(),
                    fun.getHeaders().get(AMQPMuonTransport.HEADER_SOURCE_SERVICE).toString(),
                    fun.getHeaders().get(AMQPMuonTransport.HEADER_REPLY_TO).toString());
            channelFunction.apply(handshake);
        });
    }
}
