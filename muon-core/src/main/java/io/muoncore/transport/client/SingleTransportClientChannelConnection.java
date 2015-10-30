package io.muoncore.transport.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.HashMap;
import java.util.Map;

class SingleTransportClientChannelConnection implements ChannelConnection<TransportOutboundMessage, TransportInboundMessage> {

    private TransportMessageDispatcher taps;
    private MuonTransport transport;
    private ChannelFunction<TransportInboundMessage> inbound;

    private Map<String, ChannelConnection<TransportOutboundMessage, TransportInboundMessage>> channelConnectionMap = new HashMap<>();

    public SingleTransportClientChannelConnection(
            MuonTransport transport,
            TransportMessageDispatcher taps) {
        this.transport = transport;
        this.taps = taps;
    }

    @Override
    public void receive(ChannelFunction<TransportInboundMessage> function) {
        inbound = arg -> {
            taps.dispatch(arg);
            function.apply(arg);
        };
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
        taps.dispatch(message);
        connection.send(message);
    }

    private ChannelConnection<TransportOutboundMessage, TransportInboundMessage> connectChannel(TransportOutboundMessage message) {
        ChannelConnection<TransportOutboundMessage, TransportInboundMessage> connection = transport.openClientChannel(message.getTargetServiceName(), message.getProtocol());

        connection.receive(inbound);

        return connection;
    }

    private static String key(TransportOutboundMessage key) {
        return key.getTargetServiceName() + "_" + key.getProtocol();
    }
}
