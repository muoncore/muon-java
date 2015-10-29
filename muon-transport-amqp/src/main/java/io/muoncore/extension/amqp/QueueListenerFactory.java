package io.muoncore.extension.amqp;

public interface QueueListenerFactory {
    QueueListener listenOnQueue(String queueName, QueueListener.QueueFunction function);
}
