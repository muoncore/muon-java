package io.muoncore;

import io.muoncore.codec.Codecs;
import io.muoncore.codec.TransportCodecType;
import io.muoncore.future.ImmediateReturnFuture;
import io.muoncore.future.MuonFuture;
import io.muoncore.internal.Dispatcher;
import io.muoncore.internal.MuonStreamExistingGenerator;
import io.muoncore.log.EventLogger;
import io.muoncore.spec.Operation;
import io.muoncore.spec.ServiceSpecification;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Muon implements MuonService {

    private Logger log = Logger.getLogger(Muon.class.getName());

    private Discovery discovery;

    private List<MuonEventTransport> transports = new ArrayList<MuonEventTransport>();
    private List<MuonEventTransport> nonInitTransports = new ArrayList<MuonEventTransport>();

    private TransportList<MuonResourceTransport> resourceTransports = new TransportList<MuonResourceTransport>();
    private TransportList<MuonBroadcastTransport> broadcastTransports = new TransportList<MuonBroadcastTransport>();
    private TransportList<MuonStreamTransport> streamingTransports = new TransportList<MuonStreamTransport>();
    private TransportList<MuonQueueTransport> queueingTransports = new TransportList<MuonQueueTransport>();

    private ServiceSpecification specification = new ServiceSpecification();

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

        addSpecEndpoints();
        EventLogger.initialise(this);

        started = true;
    }

    private JsonObject filterOperations(Collection<Operation> elems, Class<?> filter) {
    	JsonObject response = new JsonObject();
    	JsonArray arOps = new JsonArray();

    	for(Operation elem : elems) {
    		if(filter == null || filter.isInstance(elem)) {
    			JsonObject arElem = new JsonObject();
    			arElem.addProperty("endpoint", elem.getResource());
    			arElem.addProperty("method", elem.getClass().getSimpleName().toLowerCase());
    			// arElem.addProperty("return_type", elem.getType().getSimpleName().toLowerCase());
    			arOps.add(arElem);
    		}
    	}
    	response.add("operations", arOps);

        response.add("amqp-protocol-version", new JsonPrimitive("5"));

    	return response;
    }

    private void addSpecEndpoints() {
    	final Gson gson = new Gson();

    	// Add endpoints for schemas and specification
    	onQuery("/muon/introspect", Map.class, new MuonQuery<Map>() {
    		@Override
    		public MuonFuture<Map> onQuery(MuonResourceEvent<Map> queryEvent) {
    			JsonObject response = filterOperations(specification.getOperations(), null);
    			return new ImmediateReturnFuture<Map>(gson.fromJson(response, Map.class));
    		}
    	});

    	for(final Class<?> t : Operation.availableTypes()) {
    		String lowT = t.getSimpleName().toLowerCase();

    		onQuery("/muon/introspect/" + lowT, Map.class, new MuonQuery<Map>() {
				@Override
				public MuonFuture<?> onQuery(MuonResourceEvent<Map> queryEvent) {
					JsonObject response = filterOperations(specification.getOperations(), t);
					return new ImmediateReturnFuture<Map>(gson.fromJson(response, Map.class));
				}
    		});
    	}
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
            //command start addition of a transport, maybe not ideal, but certainly not prevented either.
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

    private <T> MuonFuture<MuonResult<T>> dispatchEvent(MuonResourceEvent ev, String resourceQuery, Class<T> type) {
        MuonResourceTransport trans = transport(ev);

        if (trans == null) {
            final MuonResult ret = new MuonResult();
            ret.setEvent(MuonResourceEventBuilder.event(null)
                    .withUri(resourceQuery)
                    .withHeader("Status", "404")
                    .withHeader("message", "No transport can be found to dispatch this message")
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

    public <T> MuonFuture<MuonResult<T>> query(String resourceQuery, Class<T> type) {
        MuonResourceEvent<T> ev = null;
        try {
            ev = resourceEvent("get", new MuonResourceEvent<T>(new URI(resourceQuery)));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return dispatchEvent(ev, resourceQuery, type);
    }

    public <T> MuonFuture<MuonResult<T>> query(MuonResourceEvent<T> payload, Class<T> type) {
        MuonResourceEvent<T> ev = resourceEvent("query", payload);
        return dispatchEvent(ev, payload.getResource(), type);
    }

    @Override
    public <T> MuonFuture<MuonResult<T>> command(String resource, MuonResourceEvent<T> payload, Class<T> type) {
        MuonResourceEvent<T> ev = resourceEvent("command", payload);
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
    	// Add operation to service specification
    	specification.addQuery(resource, type, listener);

        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(final MuonResourceTransport transport: resourceTransports.all()) {
            transport.listenOnResource(resource, "query", type, new EventResourceTransportListener<T>() {
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
    	// Add operation to service specification
    	specification.addCommand(resource, type, listener);

        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
        for(final MuonResourceTransport transport: resourceTransports.all()) {
            transport.listenOnResource(resource, "command", type, new EventResourceTransportListener<T>() {
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
    	// Add operation to service specification
    	specification.addStream(streamName, type, generator);

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

        if (uri.getQuery() != null && uri.getQuery().trim().length() > 0) {
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
}
