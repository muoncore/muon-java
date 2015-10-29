package io.muoncore.transport.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

/**
 * Front the transport layer.
 *
 * This layer will be used by all protocol channels.
 */
public interface TransportClient {
    ChannelConnection<TransportOutboundMessage, TransportInboundMessage> openClientChannel();
}
