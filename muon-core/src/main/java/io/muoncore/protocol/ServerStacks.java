package io.muoncore.protocol;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;

public interface ServerStacks {
    ChannelConnection<MuonInboundMessage, MuonOutboundMessage>
                openServerChannel(String protocol);
}
