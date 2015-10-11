package io.muoncore.transport.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.HashMap;
import java.util.Map;

class SingleTransportChannelConnection implements ChannelConnection<TransportOutboundMessage, TransportInboundMessage> {

    private MuonTransport transport;
    private ChannelFunction<TransportInboundMessage> inbound;

    private Map<String, ChannelConnection<TransportOutboundMessage, TransportInboundMessage>> channelConnectionMap = new HashMap<>();

    public SingleTransportChannelConnection(MuonTransport transport) {
        this.transport = transport;
    }

    @Override
    public void receive(ChannelFunction<TransportInboundMessage> function) {
        inbound = function;
    }

    @Override
    public void send(TransportOutboundMessage message) {
        if (inbound == null) {
            throw new IllegalStateException("Transport connection is not in a complete state can cannot send data. The receive function has not been set");
        }
        ChannelConnection<TransportOutboundMessage, TransportInboundMessage> connection = channelConnectionMap.get(
                key(message)
        );
        if (connection == null) {
            connection = connectChannel(message);
            channelConnectionMap.put(key(message), connection);
        }
        connection.send(message);
    }

    private ChannelConnection<TransportOutboundMessage, TransportInboundMessage> connectChannel(TransportOutboundMessage message) {
        ChannelConnection<TransportOutboundMessage, TransportInboundMessage> connection = transport.openClientChannel(message.getServiceName(), message.getProtocol());

        connection.receive(inbound);

        return connection;
    }

    private static String key(TransportOutboundMessage key) {
        return key.getServiceName() + "_" + key.getProtocol();
    }
}
