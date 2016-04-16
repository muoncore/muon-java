package io.muoncore.memory.transport;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;

public class InMemServer {

    private ServerStacks serverStacks;
    private String serviceName;

    public InMemServer(String serviceName, EventBus bus, ServerStacks serverStacks) {
        this.serverStacks = serverStacks;
        this.serviceName = serviceName;
        bus.register(this);
    }

    @Subscribe public void onOpenChannel(OpenChannelEvent event) {
        if (event.getTargetService().equals(serviceName)) {
            ChannelConnection<MuonInboundMessage, MuonOutboundMessage> connection =
                    serverStacks.openServerChannel(event.getProtocol());

            event.getClientChannelConnection().attachServerConnection(connection);
        }
    }
}
