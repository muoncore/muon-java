package io.muoncore.memory.transport;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.net.URI;
import java.net.URISyntaxException;

public class InMemTransport implements MuonTransport {

    private ServerStacks serverProtocols;

    public InMemTransport(ServerStacks protocols) {
        this.serverProtocols = protocols;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public String getUrlScheme() {
        return null;
    }

    @Override
    public URI getLocalConnectionURI() throws URISyntaxException {
        return null;
    }

    @Override
    public ChannelConnection<TransportOutboundMessage, TransportInboundMessage> openClientChannel(String serviceName, String protocol) {
        return null;
    }
}
