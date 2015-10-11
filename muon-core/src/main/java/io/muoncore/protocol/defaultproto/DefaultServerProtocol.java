package io.muoncore.protocol.defaultproto;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.protocol.ServerProtocol;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

public class DefaultServerProtocol implements ServerProtocol {
    @Override
    public ChannelConnection<TransportInboundMessage, TransportOutboundMessage> createChannel() {
        return null;
    }
}
