package io.muoncore;

import io.muoncore.codec.Codecs;
import io.muoncore.codec.TransportCodecType;
import io.muoncore.internal.Dispatcher;
import io.muoncore.internal.MuonStreamExistingGenerator;
import io.muoncore.transports.*;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;

public class Muon implements MuonService {

    private Logger log = Logger.getLogger(Muon.class.getName());

    private Discovery discovery;

    private List<MuonEventTransport> transports = new ArrayList<MuonEventTransport>();
    private List<MuonEventTransport> nonInitTransports = new ArrayList<MuonEventTransport>();

    private TransportList<MuonResourceTransport> resourceTransports = new TransportList<MuonResourceTransport>();
    private TransportList<MuonBroadcastTransport> broadcastTransports = new TransportList<MuonBroadcastTransport>();
    private TransportList<MuonStreamTransport> streamingTransports = new TransportList<MuonStreamTransport>();
    private TransportList<MuonQueueTransport> queueingTransports = new TransportList<MuonQueueTransport>();

    private List<MuonExtension> extensions = new ArrayList<MuonExtension>();

    private List<MuonResourceRegister> resources = new ArrayList<MuonResourceRegister>();
    private List<MuonEventRegister> events = new ArrayList<MuonEventRegister>();
    private List<MuonStreamRegister> streams = new ArrayList<MuonStreamRegister>();

    private Dispatcher dispatcher = new Dispatcher();
    private Codecs codecs = Codecs.defaults();

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
                            codecs,
                            null,
                            transports,
                            dispatcher,
                            extensions,
                            events,
                            streams,
                            resources));
        }
        for(MuonEventTransport transport: nonInitTransports) {
            initialiseTransport(transport);
        }
        discovery.advertiseLocalService(getCurrentLocalDescriptor());
        started = true;
    }

    public ServiceDescriptor getCurrentLocalDescriptor() throws URISyntaxException {

        Set<URI> resourceConnectionUris = new HashSet<URI>();

        for(MuonEventTransport t: resourceTransports.all()) {
            resourceConnectionUris.add(t.getLocalConnectionURI());
        }

        Set<URI> streamConnectionUris = new HashSet<URI>();

        for(MuonEventTransport t: streamingTransports.all()) {
            streamConnectionUris.add(t.getLocalConnectionURI());
        }

        return new ServiceDescriptor(serviceIdentifier,
                tags,
                new ArrayList<URI>(resourceConnectionUris),
                new ArrayList<URI>(streamConnectionUris));
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
    public <T> MuonResult<T> get(String resourceQuery, Class<T> type) {
        MuonResourceEvent<T> ev = null;
        try {
            ev = resourceEvent("get", new MuonResourceEvent<T>(new URI(resourceQuery)));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return dispatchEvent(ev, resourceQuery, type);
    }

    private <T> MuonResult<T> dispatchEvent(MuonResourceEvent<T> ev, String resourceQuery, Class<T> type) {
        MuonResourceTransport trans = transport(ev);
        if (trans == null) {
            MuonResult ret = new MuonResult();
            ret.setEvent(MuonResourceEventBuilder.event("")
                    .withUri(resourceQuery)
                    .withHeader("status", "404")
                    .build());
            ret.setSuccess(false);
            return ret;
        }
        encode(ev, trans.getCodecType());
        return trans.emitForReturn(resourceQuery, ev);
    }

    private <T> void encode(MuonResourceEvent<T> ev, TransportCodecType type) {
        if (ev.getDecodedContent() != null) {
            if (type == TransportCodecType.BINARY) {
                byte[] content = codecs.encodeToByte(ev.getDecodedContent());
                ev.setBinaryEncodedContent(content);
            } else {
                String content = codecs.encodeToString(ev.getDecodedContent());
                ev.setTextEncodedContent(content);
            }
        }
    }

    private <T> void decode(MuonResourceEvent<T> ev, TransportCodecType codecType, Class<T> type) {
        if (codecType== TransportCodecType.BINARY) {
            T obj = codecs.decodeObject(ev.getBinaryEncodedContent(),ev.getContentType(), type);
            ev.setDecodedContent(obj);
        } else {
            T obj = codecs.decodeObject(ev.getTextEncodedContent(),ev.getContentType(), type);
            ev.setDecodedContent(obj);
        }
    }

    private <T> void decode(MuonMessageEvent<T> ev, TransportCodecType codecType, Class<T> type) {
        if (codecType== TransportCodecType.BINARY) {
            T obj = codecs.decodeObject(ev.getBinaryEncodedContent(), ev.getContentType(), type);
            ev.setDecodedContent(obj);
        } else {
            T obj = codecs.decodeObject(ev.getTextEncodedContent(), ev.getContentType(), type);
            ev.setDecodedContent(obj);
        }
    }

    public <T> MuonResult<T> get(MuonResourceEvent<T> payload, Class<T> type) {
        MuonResourceEvent<T> ev = resourceEvent("get", payload);
        return dispatchEvent(ev, payload.getResource(), type);
    }

    @Override
    public <T> MuonResult<T> post(String resource, MuonResourceEvent<T> payload, Class<T> type) {
        MuonResourceEvent<T> ev = resourceEvent("post", payload);
        return dispatchEvent(ev, resource, type);
    }

    @Override
    public <T> MuonResult<T> put(String resource, MuonResourceEvent<T> payload, Class<T> type) {
        MuonResourceEvent<T> ev = resourceEvent("put", payload);
        return dispatchEvent(ev, resource, type);
    }

    static <T> MuonResourceEvent<T> resourceEvent(String verb, MuonResourceEvent<T> payload) {
        payload.addHeader("verb", verb);
        return payload;
    }

    @Override
    public void receive(String event, final MuonListener listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(final MuonBroadcastTransport transport: broadcastTransports.all()) {
            transport.listenOnBroadcastEvent(event, new EventMessageTransportListener() {
                @Override
                public void onEvent(String name, MuonMessageEvent obj) {
                    listener.onEvent(obj);
                }
            });
        }
    }

    @Override
    public <T> void onGet(String resource, final Class<T> type, final MuonGet<T> listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(final MuonResourceTransport transport: resourceTransports.all()) {
            transport.listenOnResource(resource, "get", type, new EventResourceTransportListener<T>() {
                @Override
                public Object onEvent(String name, MuonResourceEvent<T> obj) {
                    decode(obj, transport.getCodecType(), type);
                    return listener.onQuery(obj);
                }
            });
        }
    }

    @Override
    public <T> void onPost(String resource, final Class<T> type, final MuonPost<T> listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(final MuonResourceTransport transport: resourceTransports.all()) {
            transport.listenOnResource(resource, "post", type, new EventResourceTransportListener<T>() {
                @Override
                public Object onEvent(String name, MuonResourceEvent<T> obj) {
                    decode(obj, transport.getCodecType(), type);
                    return listener.onCommand(obj);
                }
            });
        }
    }

    @Override
    public <T> void onPut(String resource, final Class<T> type, final MuonPut<T> listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(final MuonResourceTransport transport: resourceTransports.all()) {
            transport.listenOnResource(resource, "put", type, new EventResourceTransportListener<T>() {
                @Override
                public Object onEvent(String name, MuonResourceEvent<T> obj) {
                    decode(obj, transport.getCodecType(), type);
                    return listener.onCommand(obj);
                }
            });
        }
    }

    @Override
    public <T> void onDelete(String resource, final Class<T> type, final MuonDelete<T> listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(final MuonResourceTransport transport: resourceTransports.all()) {
            transport.listenOnResource(resource, "delete", type, new EventResourceTransportListener<T>() {
                @Override
                public Object onEvent(String name, MuonResourceEvent<T> obj) {
                    decode(obj, transport.getCodecType(), type);
                    return listener.onCommand(obj);
                }
            });
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

    public static interface EventResourceTransportListener<T> {
        Object onEvent(String name, MuonResourceEvent<T> obj);
    }

    MuonResourceTransport transport(MuonResourceEvent event) {
        ServiceDescriptor remoteDescriptor = discovery.getService(event.getUri());
        if (remoteDescriptor == null) {
            return null;
        }
        return resourceTransports.findBestResourceTransport(remoteDescriptor);
    }

    /**
     * experimental streaming support
     */

    /**
     *
     * @param streamName
     */
    public void streamSource(String streamName, MuonStreamGenerator generator) {
        for(MuonStreamTransport transport: streamingTransports.all()) {
            transport.provideStreamSource(streamName, generator);
        }
    }

    public <T> void streamSource(String streamName, Publisher<T> pub) {
        streamSource(streamName, new MuonStreamExistingGenerator(pub));
    }

    /**
     * Find a remote stream to subscribe to and subscribe using the given subscriber.
     *
     * @param url the Muon url muon://serviceName/streamname
     * @param subscriber
     * @throws URISyntaxException
     */
    public <T> void subscribe(String url, Class<T> type, Subscriber<T> subscriber) throws URISyntaxException {
        subscribe(url, type, new HashMap<String, String>(), subscriber);
    }

    public <T> void subscribe(String url, Class<T> type, Map<String, String> params, Subscriber<T> subscriber) throws URISyntaxException {

        String host = new URI(url).getHost();
        ServiceDescriptor descriptor = discovery.getService(new URI(url));

        MuonStreamTransport t = streamingTransports.findBestStreamTransport(descriptor);

        if (t == null) {
            log.warning("Stream subscription to " + url + " cannot be made, no transport can connect using the connection details " + descriptor.getStreamConnectionUrls());
            subscriber.onError(new IllegalStateException("Cannot see the remote service " + host));
            return;
        }

        t.subscribeToStream(url, type, params, subscriber);
    }

    @Override
    public <T> void onQueue(String queue, final Class<T> type, final MuonListener<T> listener) {
        for(final MuonQueueTransport transport: queueingTransports.all()) {
            transport.listenOnQueueEvent(queue, type, new EventMessageTransportListener() {
                @Override
                public void onEvent(String name, MuonMessageEvent obj) {
                    decode(obj, transport.getCodecType(), type);
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
