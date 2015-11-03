package io.muoncore.extension.amqp.rabbitmq09;

import com.rabbitmq.client.Channel;
import io.muoncore.extension.amqp.QueueListener;
import io.muoncore.extension.amqp.QueueListenerFactory;

public class RabbitMq09QueueListenerFactory implements QueueListenerFactory {

    private Channel channel;

    public RabbitMq09QueueListenerFactory(Channel channel) {
        this.channel = channel;
    }

    @Override
    public QueueListener listenOnQueue(String queueName, QueueListener.QueueFunction function) {
        RabbitMq09QueueListener listener = new RabbitMq09QueueListener(channel, queueName, function);
        listener.start();
        listener.blockUntilReady();
        return listener;
    }

    @Override
    public QueueListener listenOnBroadcast(String topicName, QueueListener.QueueFunction function) {
        RabbitMq09BroadcastListener listener = new RabbitMq09BroadcastListener(channel, topicName, function);
        listener.start();
        listener.blockUntilReady();
        return listener;
    }
}
