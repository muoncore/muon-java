package io.muoncore.extension.amqp;

import io.muoncore.channel.ChannelConnection;

public interface ServiceQueue {
    void onHandshake(ChannelConnection.ChannelFunction<AmqpHandshakeMessage> channelFunction);
    void shutdown();
}
