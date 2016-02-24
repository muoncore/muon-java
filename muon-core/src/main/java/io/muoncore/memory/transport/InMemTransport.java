package io.muoncore.memory.transport;

import com.google.common.eventbus.EventBus;
import io.muoncore.Discovery;
import io.muoncore.ServiceDescriptor;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class InMemTransport implements MuonTransport {

    private EventBus bus;
    private AutoConfiguration configuration;

    private InMemServer inMemServer;
    private Discovery discovery;

    public InMemTransport(
            AutoConfiguration configuration,
            EventBus bus) {
        this.bus = bus;
        this.configuration = configuration;
    }

    @Override
    public boolean canConnectToService(String name) {
        Optional<ServiceDescriptor> descriptor = discovery
                .getKnownServices()
                .stream()
                .filter(svc ->
                        svc.getIdentifier()
                                .equals(name))
                .findFirst();

        if (!descriptor.isPresent()) return false;

        return descriptor.get().getConnectionUrls().stream().anyMatch( url -> url.getScheme().equals(getUrlScheme()));
    }

    @Override
    public void shutdown() {
        bus.unregister(inMemServer);
    }

    @Override
    public void start(Discovery discovery, ServerStacks serverStacks) throws MuonTransportFailureException {
        this.discovery = discovery;
        this.inMemServer = new InMemServer(configuration.getServiceName(), bus, serverStacks);
    }

    @Override
    public String getUrlScheme() {
        return "inmem";
    }

    @Override
    public URI getLocalConnectionURI() {
        try {
            return new URI("inmem://" + configuration.getServiceName());
        } catch (URISyntaxException e) {
            throw new MuonTransportFailureException("Incorrect URI for inmem", e);
        }
    }

    @Override
    public ChannelConnection<TransportOutboundMessage, TransportInboundMessage> openClientChannel(String serviceName, String protocol) {
        return new DefaultInMemClientChannelConnection(serviceName, protocol, bus);
    }
}
