package io.muoncore.transport.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

/**
 * Transport layer bound to a single transport.
 */
public class SingleTransportClient implements TransportClient {

    private MuonTransport transport;

    public SingleTransportClient(MuonTransport transport) {
        this.transport = transport;
    }

    @Override
    public ChannelConnection<TransportOutboundMessage, TransportInboundMessage> openClientChannel() {
        return new SingleTransportChannelConnection(transport);
    }
}
