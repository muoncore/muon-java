package io.muoncore.extension.amqp;

import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.Map;

public interface AmqpConnection {
    Channel getChannel();

    void send(QueueListener.QueueMessage message) throws IOException;

    boolean isAvailable();

    void close();
}
