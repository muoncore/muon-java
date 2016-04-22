package io.muoncore.transport.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;

/**
 * Front the transport layer.
 *
 * This layer will be used by all protocol channels.
 */
public interface TransportClient {
    ChannelConnection<MuonOutboundMessage, MuonInboundMessage> openClientChannel();
}
