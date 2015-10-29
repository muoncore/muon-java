package io.muoncore.protocol;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<ProtocolDescriptor> getProtocolDescriptors() {
        return protocols.values().stream().map(ServerProtocolStack::getProtocolDescriptor).collect(Collectors.toList());
    }

    @Override
    public void registerServerProtocol(ServerProtocolStack serverProtocolStack) {
        protocols.put(serverProtocolStack.getProtocolDescriptor().getProtocolScheme(), serverProtocolStack);
    }
}
