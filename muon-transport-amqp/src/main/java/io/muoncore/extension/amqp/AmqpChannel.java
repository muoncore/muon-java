package io.muoncore.extension.amqp;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

public interface AmqpChannel extends ChannelConnection<TransportOutboundMessage, TransportInboundMessage> {
    void initiateHandshake(String serviceName, String protocol);
    void respondToHandshake(AmqpHandshakeMessage message);

    void onShutdown(ChannelFunction runnable);
}
