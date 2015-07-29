package io.muoncore;

import com.google.common.base.Splitter;
import io.muoncore.codec.Codecs;
import io.muoncore.codec.TransportCodecType;
import io.muoncore.future.ImmediateReturnFuture;
import io.muoncore.future.MuonFuture;
import io.muoncore.internal.Dispatcher;
import io.muoncore.internal.MuonStreamExistingGenerator;
import io.muoncore.transport.MuonEventTransport;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.MuonQueueTransport;
import io.muoncore.transport.broadcast.MuonBroadcastTransport;
import io.muoncore.transport.resource.MuonResourceEvent;
import io.muoncore.transport.resource.MuonResourceEventBuilder;
import io.muoncore.transport.resource.MuonResourceTransport;
import io.muoncore.transport.stream.MuonStreamRegister;
import io.muoncore.transport.stream.MuonStreamTransport;
import io.muoncore.transport.support.TransportList;
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


//    private List<MuonResourceRegister> resources = new ArrayList<MuonResourceRegister>();
//    private List<MuonEventRegister> events = new ArrayList<MuonEventRegister>();
//    private List<MuonStreamRegister> streams = new ArrayList<MuonStreamRegister>();

    private Dispatcher dispatcher;
    private Codecs codecs = new Codecs();

    private String serviceIdentifier;

    private boolean started = false;

    private List<String> tags = new ArrayList<String>();

    public Muon(Discovery discovery) {
        this.discovery = discovery;
        dispatcher = new Dispatcher(codecs);
    }

    public void start() throws URISyntaxException {
        for(MuonEventTransport transport: nonInitTransports) {
            initialiseTransport(transport);
        }
        discovery.advertiseLocalService(getCurrentLocalDescriptor());
        started = true;
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    @Override
    public Codecs getCodecs() {
        return codecs;
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

    @Override
    public void registerTransport(MuonEventTransport transport) {
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
            //permits the addition of new transport dynamically, which may be useful.
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
    public <T> MuonFuture<MuonResult<T>> get(String resourceQuery, Class<T> type) {
        MuonResourceEvent<T> ev = null;
        try {
            ev = resourceEvent("get", new MuonResourceEvent<T>(new URI(resourceQuery)));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return dispatchEvent(ev, resourceQuery, type);
    }

    private <T> MuonFuture<MuonResult<T>> dispatchEvent(MuonResourceEvent ev, String resourceQuery, Class<T> type) {
        MuonResourceTransport trans = transport(ev);

        if (trans == null) {
            final MuonResult ret = new MuonResult();
            ret.setEvent(MuonResourceEventBuilder.event(null)
                    .withUri(resourceQuery)
                    .withHeader("status", "404")
                    .build());
            ret.setSuccess(false);

            return new ImmediateReturnFuture<MuonResult<T>>(ret);
        }
        ev.addHeader("Accept", getAcceptHeader(type));
        encode(ev, trans.getCodecType());

        MuonResult<T> ret = trans.emitForReturn(resourceQuery, ev);
        decode(ret.getResponseEvent(), trans.getCodecType(), type);

        //TODO, replace with a streaming promise version pushed all the way down through the transport layers.
        return new ImmediateReturnFuture<MuonResult<T>>(ret);
    }

    private String getAcceptHeader(Class type) {
        Set<String> acceptsContentTypes = codecs.getBinaryContentTypesAvailable(type);

        StringBuilder buf = new StringBuilder();
        for(String accept: acceptsContentTypes) {
            buf.append(accept);
            buf.append(",");
        }

        return buf.toString().substring(0, buf.length() -1);
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
        if (ev == null) return;
        if (codecType== TransportCodecType.BINARY) {
            T obj = codecs.decodeObject(ev.getBinaryEncodedContent(),ev.getContentType(), type);
            ev.setDecodedContent(obj);
        } else {
            T obj = codecs.decodeObject(ev.getTextEncodedContent(),ev.getContentType(), type);
            ev.setDecodedContent(obj);
        }
    }

    private <T> void decode(MuonMessageEvent<T> ev, TransportCodecType codecType, Class<T> type) {
        if (ev == null) return;
        if (codecType== TransportCodecType.BINARY) {
            T obj = codecs.decodeObject(ev.getBinaryEncodedContent(), ev.getContentType(), type);
            ev.setDecodedContent(obj);
        } else {
            T obj = codecs.decodeObject(ev.getTextEncodedContent(), ev.getContentType(), type);
            ev.setDecodedContent(obj);
        }
    }

    public <T> MuonFuture<MuonResult<T>> get(MuonResourceEvent<T> payload, Class<T> type) {
        MuonResourceEvent<T> ev = resourceEvent("get", payload);
        return dispatchEvent(ev, payload.getResource(), type);
    }

    @Override
    public <T> MuonFuture<MuonResult<T>> post(String resource, MuonResourceEvent<T> payload, Class<T> type) {
        MuonResourceEvent<T> ev = resourceEvent("post", payload);
        return dispatchEvent(ev, resource, type);
    }

    @Override
    public <T> MuonFuture<MuonResult<T>> put(String resource, MuonResourceEvent<T> payload, Class<T> type) {
        MuonResourceEvent<T> ev = resourceEvent("put", payload);
        return dispatchEvent(ev, resource, type);
    }

    static <T> MuonResourceEvent<T> resourceEvent(String verb, MuonResourceEvent<T> payload) {
        payload.addHeader("verb", verb);
        return payload;
    }

    @Override
    public <T> void receive(String event, final Class<T> type, final MuonListener<T> listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(final MuonBroadcastTransport transport: broadcastTransports.all()) {
            transport.listenOnBroadcastEvent(event, new EventMessageTransportListener<T>() {
                @Override
                public void onEvent(String name, MuonMessageEvent<T> obj) {
                    decode(obj, transport.getCodecType(), type);
                    listener.onEvent(obj);
                }
            });
        }
    }

    @Override
    public <T> void onQuery(String resource, final Class<T> type, final MuonQuery<T> listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(final MuonResourceTransport transport: resourceTransports.all()) {
            transport.listenOnResource(resource, "get", type, new EventResourceTransportListener<T>() {
                @Override
                public MuonFuture onEvent(String name, MuonResourceEvent<T> obj) {
                    decode(obj, transport.getCodecType(), type);
                    return listener.onQuery(obj);
                }
            });
        }
    }

    @Override
    public <T> void onCommand(String resource, final Class<T> type, final MuonCommand<T> listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(final MuonResourceTransport transport: resourceTransports.all()) {
            transport.listenOnResource(resource, "post", type, new EventResourceTransportListener<T>() {
                @Override
                public MuonFuture onEvent(String name, MuonResourceEvent<T> obj) {
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
        log.info("Muon has shutdown");
    }

    public List<ServiceDescriptor> discoverServices() {
        return discovery.getKnownServices();
    }

    public static interface EventMessageTransportListener<T> {
        void onEvent(String name, MuonMessageEvent<T> obj);
    }

    public static interface EventResourceTransportListener<T> {
        MuonFuture onEvent(String name, MuonResourceEvent<T> obj);
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

    public List<MuonStreamRegister> getStreams() {
        return null;//streams;
    }

    /**
     *
     * @param streamName
     */
    public <T> void streamSource(String streamName, Class<T> type, MuonStreamGenerator<T> generator) {
        for(MuonStreamTransport transport: streamingTransports.all()) {
            transport.provideStreamSource(streamName, generator);
        }
    }

    public <T> void streamSource(String streamName, Class<T> type, Publisher<T> pub) {
        streamSource(streamName, type, new MuonStreamExistingGenerator<T>(pub));
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

        URI uri = new URI(url);

        String host = uri.getHost();

        if (uri.getQuery() != null) {
            params.putAll(Splitter.on('&')
                    .trimResults()
                    .withKeyValueSeparator("=")
                    .split(uri.getQuery()));
        }
        ServiceDescriptor descriptor = discovery.getService(uri);

        if (descriptor == null) {
            subscriber.onError(new IllegalStateException("Service not found"));
            return;
        }

        MuonStreamTransport t = streamingTransports.findBestStreamTransport(descriptor);

        if (t == null) {
            log.warning("Stream subscription to " + url + " cannot be made, no transport can connect using the connection details " + descriptor.getStreamConnectionUrls());
            subscriber.onError(new IllegalStateException("Cannot see the remote service " + host));
            return;
        }

        t.subscribeToStream(url, type, params, subscriber);
    }

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
            throw new IllegalStateException("No transport that support queueing are configured");
        }
        MuonQueueTransport selectedTransport = queueingTransports.all().get(0);
        selectedTransport.send(event.getEventName(), event);
    }
}
