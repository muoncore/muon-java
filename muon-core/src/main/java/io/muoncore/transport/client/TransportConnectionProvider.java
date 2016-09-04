package io.muoncore.transport.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;

public interface TransportConnectionProvider {
    ChannelConnection<MuonOutboundMessage, MuonInboundMessage> connectChannel(
            String serviceName,
            String protocol,
            ChannelConnection.ChannelFunction<MuonInboundMessage> function);
}
