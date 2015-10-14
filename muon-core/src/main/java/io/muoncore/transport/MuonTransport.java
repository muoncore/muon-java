package io.muoncore.transport;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.exception.NoSuchServiceException;

import java.net.URI;
import java.net.URISyntaxException;

public interface MuonTransport {

    void shutdown();

    void start() throws MuonTransportFailureException;

    String getUrlScheme();

    URI getLocalConnectionURI() throws URISyntaxException;

    ChannelConnection<TransportOutboundMessage, TransportInboundMessage> openClientChannel(
            String serviceName,
            String protocol) throws NoSuchServiceException, MuonTransportFailureException;
}
