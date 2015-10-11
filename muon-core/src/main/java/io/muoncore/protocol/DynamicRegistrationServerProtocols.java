package io.muoncore.protocol;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.HashMap;
import java.util.Map;

public class DynamicRegistrationServerProtocols implements ServerProtocols, ServerRegistrar {

    private Map<String, ServerProtocol> protocols = new HashMap<>();
    private final ServerProtocol defaultProtocol;

    public DynamicRegistrationServerProtocols(ServerProtocol defaultProtocol) {
        this.defaultProtocol = defaultProtocol;
    }

    @Override
    public ChannelConnection<TransportInboundMessage, TransportOutboundMessage> openServerChannel(String protocol) {

        ServerProtocol proto = protocols.get(protocol);

        if (proto == null) {
            proto = defaultProtocol;
        }

        return proto.createChannel();
    }

    @Override
    public void registerServerProtocol(String protocolName, ServerProtocol serverProtocol) {
        protocols.put(protocolName, serverProtocol);
    }
}
