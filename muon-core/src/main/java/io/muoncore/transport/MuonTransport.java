package io.muoncore.transport;

import io.muoncore.Discovery;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.exception.NoSuchServiceException;
import io.muoncore.protocol.ServerStacks;

import java.net.URI;

public interface MuonTransport {

    void shutdown();

    void start(
            Discovery discovery,
            ServerStacks serverStacks) throws MuonTransportFailureException;

    String getUrlScheme();

    URI getLocalConnectionURI();

    ChannelConnection<TransportOutboundMessage, TransportInboundMessage> openClientChannel(
            String serviceName,
            String protocol) throws NoSuchServiceException, MuonTransportFailureException;
}
