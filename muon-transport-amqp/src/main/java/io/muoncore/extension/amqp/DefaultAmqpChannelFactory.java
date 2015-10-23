package io.muoncore.extension.amqp;

public class DefaultAmqpChannelFactory implements AmqpChannelFactory {

    private String localServiceName;
    private QueueListenerFactory listenerFactory;
    private AmqpConnection connection;

    public DefaultAmqpChannelFactory(String localServiceName, QueueListenerFactory listenerFactory, AmqpConnection connection) {
        this.localServiceName = localServiceName;
        this.listenerFactory = listenerFactory;
        this.connection = connection;
    }

    @Override
    public AmqpChannel createChannel() {
        return new DefaultAmqpChannel(connection, listenerFactory, localServiceName);
    }
}
