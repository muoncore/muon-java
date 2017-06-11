package io.muoncore.memory.transport;

import com.google.common.eventbus.EventBus;
import io.muoncore.Discovery;
import io.muoncore.ServiceDescriptor;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.support.Scheduler;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.transport.MuonTransport;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;

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
                .getServiceNamed(name);

        if (!descriptor.isPresent()) return false;

        return descriptor.get().getSchemes().stream().anyMatch( url -> url.equals(getUrlScheme()));
    }

    @Override
    public void shutdown() {
        bus.unregister(inMemServer);
    }

    @Override
    public void start(Discovery discovery, ServerStacks serverStacks, Codecs codecs, Scheduler scheduler) throws MuonTransportFailureException {
        this.discovery = discovery;
        this.inMemServer = new InMemServer(configuration.getServiceName(), bus, serverStacks);
    }

    @Override
    public String getUrlScheme() {
        return "inmem";
    }

    public void triggerFailure() {
      bus.post(new InMemFailureEvent());
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
    public ChannelConnection<MuonOutboundMessage, MuonInboundMessage> openClientChannel(String serviceName, String protocol) {
        return new DefaultInMemClientChannelConnection(serviceName, protocol, bus);
    }
}
