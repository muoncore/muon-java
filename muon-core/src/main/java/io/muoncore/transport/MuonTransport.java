package io.muoncore.transport;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.exception.NoSuchServiceException;
import io.muoncore.protocol.ServerStacks;

import java.net.URI;
import java.net.URISyntaxException;

public interface MuonTransport {

    void shutdown();

    void start(ServerStacks serverStacks) throws MuonTransportFailureException;

    String getUrlScheme();

    URI getLocalConnectionURI() throws URISyntaxException;

    ChannelConnection<TransportOutboundMessage, TransportInboundMessage> openClientChannel(
            String serviceName,
            String protocol) throws NoSuchServiceException, MuonTransportFailureException;
}
