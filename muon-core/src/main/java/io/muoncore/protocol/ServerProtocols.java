package io.muoncore.protocol;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

public interface ServerProtocols {
    ChannelConnection<TransportInboundMessage, TransportOutboundMessage>
                openServerChannel(String protocol);
}
