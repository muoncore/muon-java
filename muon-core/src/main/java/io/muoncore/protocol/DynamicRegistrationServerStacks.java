package io.muoncore.protocol;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.HashMap;
import java.util.Map;

public class DynamicRegistrationServerStacks implements ServerStacks, ServerRegistrar {

    private Map<String, ServerProtocolStack> protocols = new HashMap<>();
    private final ServerProtocolStack defaultProtocol;

    public DynamicRegistrationServerStacks(ServerProtocolStack defaultProtocol) {
        this.defaultProtocol = defaultProtocol;
    }

    @Override
    public ChannelConnection<TransportInboundMessage, TransportOutboundMessage> openServerChannel(String protocol) {

        ServerProtocolStack proto = protocols.get(protocol);

        if (proto == null) {
            proto = defaultProtocol;
        }

        return proto.createChannel();
    }

    @Override
    public void registerServerProtocol(String protocolName, ServerProtocolStack serverProtocolStack) {
        protocols.put(protocolName, serverProtocolStack);
    }
}
