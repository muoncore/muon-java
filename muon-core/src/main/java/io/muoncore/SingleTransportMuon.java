package io.muoncore;

import io.muoncore.codec.Codecs;
import io.muoncore.codec.crypt.EncryptingCodecs;
import io.muoncore.codec.crypt.SymmetricAESEncryptionAlgorithm;
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
import io.muoncore.protocol.support.ProtocolTimer;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportControl;
import io.muoncore.transport.client.SimpleTransportMessageDispatcher;
import io.muoncore.transport.client.SingleTransportClient;
import io.muoncore.transport.client.TransportClient;
import io.muoncore.transport.client.TransportMessageDispatcher;
import reactor.Environment;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * Simple bundle of default Muon protocol stacks based on a single transport.
 */
public class SingleTransportMuon implements Muon, ServerRegistrarSource {

    private SingleTransportClient transportClient;
    private TransportControl transportControl;
    private Discovery discovery;
    private ServerStacks protocols;
    private ServerRegistrar registrar;
    private RequestResponseHandlers requestResponseHandlers;
    private Codecs codecs;
    private AutoConfiguration configuration;
    private PublisherLookup publisherLookup;
    private ProtocolTimer protocolTimer;

    public SingleTransportMuon(
            AutoConfiguration configuration,
            Discovery discovery,
            MuonTransport transport) {
        Environment.initializeIfEmpty();
        this.configuration = configuration;
        TransportMessageDispatcher wiretap = new SimpleTransportMessageDispatcher();
        SingleTransportClient client = new SingleTransportClient(
                transport, wiretap);
        this.transportClient = client;
        this.transportControl = client;
        this.discovery = discovery;
        this.protocolTimer = new ProtocolTimer();
        this.publisherLookup = new DefaultPublisherLookup();

        this.codecs = new EncryptingCodecs(
                new JsonOnlyCodecs(),
                new SymmetricAESEncryptionAlgorithm(configuration.getAesEncryptionKey()));

        DynamicRegistrationServerStacks stacks = new DynamicRegistrationServerStacks(
                new DefaultServerProtocol(codecs),
                wiretap);
        this.protocols = stacks;
        this.registrar = stacks;

        initDefaultRequestHandler();

        initServerStacks(stacks);

        transport.start(discovery, stacks);

        discovery.advertiseLocalService(new ServiceDescriptor(
                configuration.getServiceName(),
                configuration.getTags(),
                Arrays.asList(codecs.getAvailableCodecs()),
                Collections.singletonList(transport.getLocalConnectionURI()
                )));

        discovery.blockUntilReady();
    }

    @Override
    public ServerRegistrar getProtocolStacks() {
        return registrar;
    }

    private void initServerStacks(DynamicRegistrationServerStacks stacks) {
        stacks.registerServerProtocol(new RequestResponseServerProtocolStack(
                        requestResponseHandlers, codecs, discovery));

        stacks.registerServerProtocol(new ReactiveStreamServerStack(getPublisherLookup(), getCodecs(), configuration));
        stacks.registerServerProtocol(new IntrospectionServerProtocolStack(
                () -> new ServiceExtendedDescriptor(configuration.getServiceName(), registrar.getProtocolDescriptors()),
                codecs));
    }

    private void initDefaultRequestHandler() {
        this.requestResponseHandlers = new DynamicRequestResponseHandlers(new RequestResponseServerHandler<Map, Map>() {

            @Override
            public HandlerPredicate getPredicate() {
                return HandlerPredicates.none();
            }

            @Override
            public void handle(RequestWrapper<Map> request) {
                request.notFound();
            }

            @Override
            public Type getRequestType() {
                return Map.class;
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
        transportClient.shutdown();
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
    public ProtocolTimer getProtocolTimer() {
        return protocolTimer;
    }
}
