package org.muoncore;

import org.muoncore.filter.EventFilterChain;
import org.muoncore.internal.Dispatcher;
import org.muoncore.internal.MuonStreamExistingGenerator;
import org.muoncore.transports.*;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;

public class Muon implements MuonService {

    private Logger log = Logger.getLogger(Muon.class.getName());

    private Discovery discovery;

    private List<EventFilterChain> filterChains = new ArrayList<EventFilterChain>();
    private List<MuonEventTransport> transports = new ArrayList<MuonEventTransport>();
    private List<MuonEventTransport> nonInitTransports = new ArrayList<MuonEventTransport>();

    private TransportList<MuonResourceTransport> resourceTransports = new TransportList<MuonResourceTransport>();
    private TransportList<MuonBroadcastTransport> broadcastTransports = new TransportList<MuonBroadcastTransport>();
    private TransportList<MuonStreamTransport> streamingTransports = new TransportList<MuonStreamTransport>();
    private TransportList<MuonQueueTransport> queueingTransports = new TransportList<MuonQueueTransport>();

    private List<MuonExtension> extensions = new ArrayList<MuonExtension>();

    private List<MuonResourceRegister> resources = new ArrayList<MuonResourceRegister>();
    private List<MuonEventRegister> events = new ArrayList<MuonEventRegister>();

    private Dispatcher dispatcher = new Dispatcher();

    private String serviceIdentifier;

    private boolean started = false;

    private List<String> tags = new ArrayList<String>();

    @Override
    public void registerExtension(MuonExtension extension) {
        extensions.add(extension);
    }

    public Muon(Discovery discovery) {
        this.discovery = discovery;
    }

    public void start() throws URISyntaxException {
        for (MuonExtension extension: extensions) {
            extension.init(
                    new MuonExtensionApi(
                            this,
                            null,
                            filterChains,
                            transports,
                            dispatcher,
                            extensions,
                            events,
                            resources));
        }
        for(MuonEventTransport transport: nonInitTransports) {
            initialiseTransport(transport);
        }
        discovery.advertiseLocalService(getCurrentLocalDescriptor());
        started = true;
    }

    public ServiceDescriptor getCurrentLocalDescriptor() throws URISyntaxException {

        Set<URI> connectionUris = new HashSet<URI>();

        for(MuonEventTransport t: transports) {
            connectionUris.add(t.getLocalConnectionURI());
        }

        return new ServiceDescriptor(serviceIdentifier,
                tags,
                new ArrayList<URI>(connectionUris));
    }

    void registerTransport(MuonEventTransport transport) {
        transports.add(transport);

        if(transport instanceof MuonResourceTransport) {
            resourceTransports.addTransport((MuonResourceTransport) transport);
        }
        if(transport instanceof MuonBroadcastTransport) {
            broadcastTransports.addTransport((MuonBroadcastTransport) transport);
        }
        if(transport instanceof MuonStreamTransport) {
            streamingTransports.addTransport((MuonStreamTransport) transport);
        }
        if (transport instanceof MuonQueueTransport) {
            queueingTransports.addTransport((MuonQueueTransport) transport);
        }

        if (!started) {
            nonInitTransports.add(transport);
        } else {
            //post start addition of a transport, maybe not ideal, but certainly not prevented either.
            //permits the addition of new transports dynamically, which may be useful.
            initialiseTransport(transport);
        }
    }

    private void initialiseTransport(MuonEventTransport transport) {
        try {
            transport.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Muon: Starting transport " + transport.getClass().getSimpleName());
    }

    public String getServiceIdentifer() {
        return serviceIdentifier;
    }

    public void setServiceIdentifer(String serviceIdentifer) {
        this.serviceIdentifier = serviceIdentifer;
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public void addTags(String ... tag) {
        this.tags.addAll(Arrays.asList(tag));
    }

    @Override
    public void emit(MuonMessageEvent ev) {
        dispatcher.dispatchToTransports(ev, broadcastTransports.all());
    }

    @Override
    public MuonResult get(String resourceQuery) {
        MuonResourceEvent ev = null;
        try {
            ev = resourceEvent("get", new MuonResourceEvent(new URI(resourceQuery), "application/json", "{}"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return transport(ev).emitForReturn(resourceQuery, ev);
    }

    public MuonResult get(MuonResourceEvent payload) {
        MuonResourceEvent ev = resourceEvent("get", payload);
        return transport(ev).emitForReturn(payload.getResource(), ev);
    }

    @Override
    public MuonResult post(String resource, MuonResourceEvent payload) {
        MuonResourceEvent ev = resourceEvent("post", payload);
        return transport(ev).emitForReturn(resource, ev);
    }

    @Override
    public MuonResult put(String resource, MuonResourceEvent payload) {
        MuonResourceEvent ev = resourceEvent("put", payload);

        return transport(ev).emitForReturn(resource, ev);
    }

    static MuonResourceEvent resourceEvent(String verb, MuonResourceEvent payload) {
        payload.addHeader("verb", verb);
        return payload;
    }

    @Override
    public void receive(String event, final MuonListener listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(MuonEventTransport transport: transports) {
            if (transport instanceof MuonBroadcastTransport) {
                ((MuonBroadcastTransport) transport).listenOnBroadcastEvent(event, new EventMessageTransportListener() {
                    @Override
                    public void onEvent(String name, MuonMessageEvent obj) {
                        listener.onEvent(obj);
                    }
                });
            }
        }
    }

    @Override
    public void onGet(String resource, String descriptor, final MuonGet listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(MuonEventTransport transport: transports) {
            if (transport instanceof MuonResourceTransport) {
                ((MuonResourceTransport) transport).listenOnResource(resource, "get", new EventResourceTransportListener() {
                    @Override
                    public Object onEvent(String name, MuonResourceEvent obj) {
                        return listener.onQuery(obj);
                    }
                });
            }
        }
    }

    @Override
    public void onPost(String resource, String descriptor, final MuonPost listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(MuonEventTransport transport: transports) {
            if (transport instanceof MuonResourceTransport) {
                ((MuonResourceTransport) transport).listenOnResource(resource, "post", new EventResourceTransportListener() {
                    @Override
                    public Object onEvent(String name, MuonResourceEvent obj) {
                        return listener.onCommand(obj);
                    }
                });
            }
        }
    }

    @Override
    public void onPut(String resource, String descriptor, final MuonPut listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(MuonEventTransport transport: transports) {
            if (transport instanceof MuonResourceTransport) {
                ((MuonResourceTransport) transport).listenOnResource(resource, "put", new EventResourceTransportListener() {
                    @Override
                    public Object onEvent(String name, MuonResourceEvent obj) {
                        return listener.onCommand(obj);
                    }
                });
            }
        }
    }

    @Override
    public void onDelete(String resource, String descriptor, final MuonDelete listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(MuonEventTransport transport: transports) {
            if (transport instanceof MuonResourceTransport) {
                ((MuonResourceTransport) transport).listenOnResource(resource, "delete", new EventResourceTransportListener() {
                    @Override
                    public Object onEvent(String name, MuonResourceEvent obj) {
                        return listener.onCommand(obj);
                    }
                });
            }
        }
    }

    @Override
    public void shutdown() {
        for(MuonEventTransport transport: transports) {
            transport.shutdown();
        }
    }

    public List<ServiceDescriptor> discoverServices() {
        return discovery.getKnownServices();
    }

    public static interface EventMessageTransportListener {
        void onEvent(String name, MuonMessageEvent obj);
    }

    public static interface EventResourceTransportListener {
        Object onEvent(String name, MuonResourceEvent obj);
    }

    MuonResourceTransport transport(MuonResourceEvent event) {
        ServiceDescriptor remoteDescriptor = discovery.getService(event.getUri());
        return resourceTransports.findBestTransport(remoteDescriptor);
    }

    /**
     * experimental streaming support
     */

    public void streamSink(String url, Subscriber pub) {
        throw new IllegalStateException("Not implemented yet");
        //need to fully define the semantics of a push connection of Publisher to Subscriber
        //is this a good idea?
//        streamer.provideStreamSink(url, pub);
    }

    /**
     *
     * @param streamName
     */
    public void streamSource(String streamName, MuonStreamGenerator generator) {
        for(MuonStreamTransport transport: streamingTransports.all()) {
            transport.provideStreamSource(streamName, generator);
        }
    }

    public void streamSource(String streamName, Publisher pub) {
        streamSource(streamName, new MuonStreamExistingGenerator(pub));
    }

    /**
     * Find a remote stream to subscribe to and subscribe using the given subscriber.
     *
     * @param url the Muon url muon://serviceName/streamname
     * @param subscriber
     * @throws URISyntaxException
     */
    public void subscribe(String url, Subscriber subscriber) throws URISyntaxException {
        subscribe(url, new HashMap<String, String>(), subscriber);
    }

    public void subscribe(String url, Map<String, String> params, Subscriber subscriber) throws URISyntaxException {

        String host = new URI(url).getHost();
        ServiceDescriptor descriptor = discovery.getService(new URI(url));

        MuonStreamTransport t = streamingTransports.findBestTransport(descriptor);

        if (t == null) {
            subscriber.onError(new IllegalStateException("Cannot see the remote service " + host));
            return;
        }

        t.subscribeToStream(url,params, subscriber);
    }

    public void publish(String url, Publisher publisher) {
        //need to fully define the semantics of a push connection of Publisher to Subscriber
        //is this a good idea?
        throw new IllegalStateException("Not implemented yet");
//        streamer.publishToStream(url, publisher);
    }

    @Override
    public void onQueue(String queue, final MuonListener listener) {
        for(MuonQueueTransport transport: queueingTransports.all()) {
            transport.listenOnQueueEvent(queue, new EventMessageTransportListener() {
                @Override
                public void onEvent(String name, MuonMessageEvent obj) {
                    listener.onEvent(obj);
                }
            });
        }
    }

    @Override
    public void sendMessage(MuonMessageEvent event) {
        //this selects the transport to choose to send this message
        //it will only be sent on one, and that will be the first one.

        if (queueingTransports.all().size() == 0) {
            throw new IllegalStateException("No transports that support queueing are configured");
        }
        MuonQueueTransport selectedTransport = queueingTransports.all().get(0);
        selectedTransport.send(event.getEventName(), event);
    }
}
