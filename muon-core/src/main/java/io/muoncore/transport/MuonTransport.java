package io.muoncore.transport;

import io.muoncore.channel.ChannelConnection;

import java.net.URI;
import java.net.URISyntaxException;

public interface MuonTransport {

    void shutdown();

    void start() throws Exception;

    String getUrlScheme();

    URI getLocalConnectionURI() throws URISyntaxException;

    ChannelConnection<TransportOutboundMessage, TransportInboundMessage> inboundChannel(String name);
    ChannelConnection<TransportOutboundMessage, TransportInboundMessage> channelToRemote(String remoteName, String channelName);
}
