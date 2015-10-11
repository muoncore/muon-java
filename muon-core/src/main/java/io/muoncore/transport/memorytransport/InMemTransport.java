package io.muoncore.transport.memorytransport;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.protocol.ServerProtocols;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.net.URI;
import java.net.URISyntaxException;

public class InMemTransport implements MuonTransport {

    private ServerProtocols serverProtocols;

    public InMemTransport(ServerProtocols protocols) {
        this.serverProtocols = protocols;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void start() throws Exception {
        ChannelConnection<TransportInboundMessage, TransportOutboundMessage> connection = serverProtocols.openServerChannel("requestresponse");

        connection.receive( message -> System.out.println(message.getId()));
        connection.send(new TransportInboundMessage("id", "serviceId", "channelName"));
    }

    @Override
    public String getUrlScheme() {
        return null;
    }

    @Override
    public URI getLocalConnectionURI() throws URISyntaxException {
        return null;
    }
}
