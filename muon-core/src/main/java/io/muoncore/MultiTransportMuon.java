package io.muoncore;

import io.muoncore.channel.Channels;
import io.muoncore.channel.support.Scheduler;
import io.muoncore.codec.Codecs;
import io.muoncore.codec.DelegatingCodecs;
import io.muoncore.codec.avro.AvroCodec;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.descriptors.ServiceExtendedDescriptor;
import io.muoncore.protocol.DynamicRegistrationServerStacks;
import io.muoncore.protocol.ServerRegistrar;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.protocol.defaultproto.DefaultServerProtocol;
import io.muoncore.protocol.introspection.server.IntrospectionServerProtocolStack;
import io.muoncore.protocol.reactivestream.server.DefaultPublisherLookup;
import io.muoncore.protocol.reactivestream.server.PublisherLookup;
import io.muoncore.protocol.reactivestream.server.ReactiveStreamServerStack;
import io.muoncore.protocol.requestresponse.server.*;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportControl;
import io.muoncore.transport.client.MultiTransportClient;
import io.muoncore.transport.client.SimpleTransportMessageDispatcher;
import io.muoncore.transport.client.TransportClient;
import io.muoncore.transport.client.TransportMessageDispatcher;
import io.muoncore.transport.sharedsocket.client.SharedSocketRouter;
import io.muoncore.transport.sharedsocket.server.SharedChannelServerStacks;
import reactor.Environment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple bundle of default Muon protocol stacks
 */
public class MultiTransportMuon implements Muon, ServerRegistrarSource {

    private MultiTransportClient transportClient;
    private TransportControl transportControl;
    private Discovery discovery;
    private ServerStacks protocols;
    private ServerRegistrar registrar;
    private RequestResponseHandlers requestResponseHandlers;
    private Codecs codecs;
    private AutoConfiguration configuration;
    private PublisherLookup publisherLookup;
    private Scheduler protocolTimer;

    public MultiTransportMuon(
            AutoConfiguration configuration,
            Discovery discovery,
            List<MuonTransport> transports, Codecs codecs) {
        Environment.initializeIfEmpty();
        this.configuration = configuration;
        this.codecs = new DelegatingCodecs().withCodec(new AvroCodec()).withCodecs(codecs);
        TransportMessageDispatcher wiretap = new SimpleTransportMessageDispatcher();
        MultiTransportClient client = new MultiTransportClient(
                transports, wiretap, configuration, discovery, codecs);
        this.transportClient = client;
        this.transportControl = client;
        this.discovery = discovery;
        this.protocolTimer = new Scheduler();
        this.publisherLookup = new DefaultPublisherLookup();

        DynamicRegistrationServerStacks stacks = new DynamicRegistrationServerStacks(
                new DefaultServerProtocol(codecs, configuration, discovery),
                wiretap);
        this.protocols = new SharedChannelServerStacks(stacks, codecs);
        this.registrar = stacks;

        initDefaultRequestHandler();

        initServerStacks(stacks);

        transports.forEach(tr -> tr.start(discovery, this.protocols, codecs, getScheduler()));

        discovery.advertiseLocalService(new ServiceDescriptor(
                configuration.getServiceName(),
                configuration.getTags(),
                Arrays.asList(codecs.getAvailableCodecs()),
                transports.stream().map(MuonTransport::getLocalConnectionURI).collect(Collectors.toList()),
                generateCapabilities()));

        discovery.blockUntilReady();
    }

    private Set<String> generateCapabilities() {
        Set<String> capabilities = new HashSet<>();
        capabilities.add(SharedSocketRouter.PROTOCOL);
        return capabilities;
    }

    @Override
    public ServerRegistrar getProtocolStacks() {
        return registrar;
    }

    private void initServerStacks(DynamicRegistrationServerStacks stacks) {
        stacks.registerServerProtocol(new RequestResponseServerProtocolStack(
                        requestResponseHandlers, codecs, discovery, configuration));

        stacks.registerServerProtocol(new ReactiveStreamServerStack(getPublisherLookup(), getCodecs(), configuration, discovery));
        stacks.registerServerProtocol(new IntrospectionServerProtocolStack(
                () -> new ServiceExtendedDescriptor(configuration.getServiceName(), registrar.getProtocolDescriptors()),
                codecs, discovery));
    }

    private void initDefaultRequestHandler() {
        this.requestResponseHandlers = new DynamicRequestResponseHandlers(new RequestResponseServerHandler() {

            @Override
            public HandlerPredicate getPredicate() {
                return HandlerPredicates.none();
            }

            @Override
            public void handle(RequestWrapper request) {
                request.notFound();
            }
        });
    }

    @Override
    public Codecs getCodecs() {
        return codecs;
    }

    @Override
    public Discovery getDiscovery() {
        return discovery;
    }

    @Override
    public RequestResponseHandlers getRequestResponseHandlers() {
        return requestResponseHandlers;
    }

    @Override
    public TransportClient getTransportClient() {
        return transportClient;
    }

    @Override
    public AutoConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void shutdown() {
        discovery.shutdown();
        transportClient.shutdown();
        Channels.shutdown();
    }

    @Override
    public TransportControl getTransportControl() {
        return transportControl;
    }

    @Override
    public PublisherLookup getPublisherLookup() {
        return publisherLookup;
    }

    @Override
    public Scheduler getScheduler() {
        return protocolTimer;
    }
}
