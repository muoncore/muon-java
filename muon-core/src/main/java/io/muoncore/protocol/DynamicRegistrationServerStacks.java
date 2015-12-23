package io.muoncore.protocol;

import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;
import io.muoncore.transport.client.TransportMessageDispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamicRegistrationServerStacks implements ServerStacks, ServerRegistrar {

    private Map<String, ServerProtocolStack> protocols = new HashMap<>();
    private final ServerProtocolStack defaultProtocol;
    private final TransportMessageDispatcher wiretapDispatch;

    public DynamicRegistrationServerStacks(
            ServerProtocolStack defaultProtocol,
            TransportMessageDispatcher wiretapDispatch) {
        this.defaultProtocol = defaultProtocol;
        this.wiretapDispatch = wiretapDispatch;
    }

    @Override
    public ChannelConnection<TransportInboundMessage, TransportOutboundMessage> openServerChannel(String protocol) {

        ServerProtocolStack proto = protocols.get(protocol);

        if (proto == null) {
            proto = defaultProtocol;
        }

        Channel<TransportInboundMessage, TransportOutboundMessage> tap = Channels.wiretapChannel(wiretapDispatch);

        Channels.connect(tap.right(), proto.createChannel());

        return tap.left();
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
