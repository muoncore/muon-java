package io.muoncore.transport;

import io.muoncore.channel.ChannelConnection;

import java.net.URI;
import java.net.URISyntaxException;

public interface MuonTransport {

    void shutdown();

    void start() throws Exception;

    String getUrlScheme();

    URI getLocalConnectionURI() throws URISyntaxException;
}
