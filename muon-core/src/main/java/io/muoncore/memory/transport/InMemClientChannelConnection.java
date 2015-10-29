package io.muoncore.memory.transport;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

public interface InMemClientChannelConnection extends ChannelConnection<TransportOutboundMessage, TransportInboundMessage> {
    void attachServerConnection(ChannelConnection<TransportInboundMessage, TransportOutboundMessage> serverChannel);
}
