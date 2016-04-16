package io.muoncore.memory.transport;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;

public interface InMemClientChannelConnection extends ChannelConnection<MuonOutboundMessage, MuonInboundMessage> {
    void attachServerConnection(ChannelConnection<MuonInboundMessage, MuonOutboundMessage> serverChannel);
}
