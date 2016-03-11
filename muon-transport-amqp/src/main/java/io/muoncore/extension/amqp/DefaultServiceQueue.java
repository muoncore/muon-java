package io.muoncore.extension.amqp;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultServiceQueue implements ServiceQueue {

    private Logger LOG = Logger.getLogger(ServiceQueue.class.getCanonicalName());
    private RabbitMq09QueueListener listener;
    private String serviceName;
    private AmqpConnection connection;

    public DefaultServiceQueue(String serviceName, AmqpConnection connection) {
        this.serviceName = serviceName;
        this.connection = connection;
    }

    @Override
    public void shutdown() {
        listener.cancel();
        try { connection.close(); } catch (Exception ignored){}
    }

    @Override
    public void onHandshake(ChannelConnection.ChannelFunction<AmqpHandshakeMessage> channelFunction) {
        if (listener != null) throw new IllegalStateException("QueueListener already has a handshake.");

        listener = new RabbitMq09QueueListener(connection.getChannel(), "service." + serviceName, fun -> {

            if (!isvalid(AMQPMuonTransport.HEADER_PROTOCOL, fun)) {
                LOG.log(Level.SEVERE, "Handshake header missing " + AMQPMuonTransport.HEADER_PROTOCOL);
            }
            if (!isvalid(AMQPMuonTransport.HEADER_SOURCE_SERVICE, fun)) {
                LOG.log(Level.SEVERE, "Handshake header missing " + AMQPMuonTransport.HEADER_SOURCE_SERVICE);
            }
            if (!isvalid(AMQPMuonTransport.HEADER_RECEIVE_QUEUE, fun)) {
                LOG.log(Level.SEVERE, "Handshake header missing " + AMQPMuonTransport.HEADER_RECEIVE_QUEUE);
            }
            if (!isvalid(AMQPMuonTransport.HEADER_REPLY_TO, fun)) {
                LOG.log(Level.SEVERE, "Handshake header missing " + AMQPMuonTransport.HEADER_REPLY_TO);
            }
            AmqpHandshakeMessage handshake = new AmqpHandshakeMessage(
                    fun.getHeaders().get(AMQPMuonTransport.HEADER_PROTOCOL).toString(),
                    fun.getHeaders().get(AMQPMuonTransport.HEADER_SOURCE_SERVICE).toString(),
                    fun.getHeaders().get(AMQPMuonTransport.HEADER_REPLY_TO).toString(),
                    fun.getHeaders().get(AMQPMuonTransport.HEADER_RECEIVE_QUEUE).toString());
            channelFunction.apply(handshake);
        });
        listener.start();
        listener.blockUntilReady();
    }
    
    static boolean isvalid(String name, QueueListener.QueueMessage message) {
        return message.getHeaders().get(name) != null;
    }
}
