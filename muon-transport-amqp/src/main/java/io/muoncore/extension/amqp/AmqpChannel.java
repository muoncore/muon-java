package io.muoncore.extension.amqp;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;

public interface AmqpChannel extends ChannelConnection<MuonOutboundMessage, MuonInboundMessage> {
    void initiateHandshake(String serviceName, String protocol);
    void respondToHandshake(AmqpHandshakeMessage message);

    void onShutdown(ChannelFunction runnable);
}
