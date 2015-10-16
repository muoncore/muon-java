package io.muoncore.extension.amqp;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

/**
 * Created by david on 16/10/15.
 */
public interface AmqpChannel extends ChannelConnection<TransportOutboundMessage, TransportInboundMessage> {
    void initiateHandshake(String serviceName, String protocol);

    void respondToHandshake(AmqpHandshakeMessage message);
}
