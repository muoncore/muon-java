package io.muoncore.extension.amqp;

import com.rabbitmq.client.Channel;

import java.io.IOException;

public interface AmqpConnection {
    Channel getChannel();

    void send(QueueListener.QueueMessage message) throws IOException;
    void broadcast(QueueListener.QueueMessage message) throws IOException;

    boolean isAvailable();

    void close();
    void deleteQueue(String queue);
}
