package org.muoncore;

import org.muoncore.filter.EventFilterChain;
import org.muoncore.transport.AMQPEventTransport;
import org.muoncore.transport.HttpEventTransport;
import org.muoncore.transport.LocalEventTransport;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class TransportedMuon implements MuonService {

    List<EventFilterChain> filterChains = new ArrayList<EventFilterChain>();
    List<MuonEventTransport> transports = new ArrayList<MuonEventTransport>();

    List<MuonExtension> extensions = new ArrayList<MuonExtension>();

    Dispatcher dispatcher = new Dispatcher();

    public TransportedMuon() {
//        setupLocalTransport();
        setupAMQPTransport();
        setupHttpTransport();
    }

    @Override
    public void registerExtension(MuonExtension extension) {
        extensions.add(extension);
        extension.init(
                new MuonExtensionApi(
                        this,
                        filterChains,
                        transports,
                        dispatcher,
                        extensions));
    }

    private void setupHttpTransport() {
        try {
            MuonEventTransport trans = new HttpEventTransport();
            transports.add(trans);
            EventFilterChain chain = new EventFilterChain(trans);
            filterChains.add(chain);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupLocalTransport() {
        try {
            MuonEventTransport trans = new LocalEventTransport();
            transports.add(trans);
            EventFilterChain chain = new EventFilterChain(trans);
            filterChains.add(chain);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupAMQPTransport() {
        try {
            MuonEventTransport trans = new AMQPEventTransport();
            transports.add(trans);
            EventFilterChain chain = new EventFilterChain(trans);
            filterChains.add(chain);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void emit(String eventName, Object payload) {
        MuonEvent ev = new MuonEvent(eventName, payload);
        dispatcher.dispatchToTransports(ev, transports(ev));
    }

    @Override
    public MuonResult get(String resourceQuery) {
        MuonEvent ev = resourceEvent(resourceQuery, "get", "");
        return transport(ev).emitForReturn(resourceQuery, ev);
    }

    @Override
    public MuonResult post(String resource, Object payload) {
        MuonEvent ev = resourceEvent(resource, "post", payload);
        return transport(ev).emitForReturn(resource, ev);
    }

    @Override
    public MuonResult put(String resource, Object payload) {
        MuonEvent ev = resourceEvent(resource, "put", payload);

        return transport(ev).emitForReturn(resource, ev);
    }

    @Override
    public void receive(String event, final MuonListener listener) {
        for(MuonEventTransport transport: transports) {
            transport.listenOnEvent(event, new EventTransportListener() {
                @Override
                public Object onEvent(String name, Object obj) {
                    listener.onEvent(obj);
                    return null;
                }
            });
        }
    }

    @Override
    public void resource(String resource, String descriptor, final MuonGet listener) {
        for(MuonEventTransport transport: transports) {
            transport.listenOnResource(resource, "get", new EventTransportListener() {
                @Override
                public Object onEvent(String name, Object obj) {
                    return listener.onQuery(obj);
                }
            });
        }
    }

    @Override
    public void resource(String resource, String descriptor, final MuonPost listener) {
        for(MuonEventTransport transport: transports) {
            transport.listenOnResource(resource, "post", new EventTransportListener() {
                @Override
                public Object onEvent(String name, Object obj) {
                    return listener.onCommand(obj);
                }
            });
        }
    }

    @Override
    public void resource(String resource, String descriptor, final MuonPut listener) {
        for(MuonEventTransport transport: transports) {
            transport.listenOnResource(resource, "put", new EventTransportListener() {
                @Override
                public Object onEvent(String name, Object obj) {
                    return listener.onCommand(obj);
                }
            });
        }
    }

    @Override
    public void resource(String resource, String descriptor, final MuonDelete listener) {
        for(MuonEventTransport transport: transports) {
            transport.listenOnResource(resource, "delete", new EventTransportListener() {
                @Override
                public Object onEvent(String name, Object obj) {
                    return listener.onCommand(obj);
                }
            });
        }
    }

    public static interface EventTransportListener {
        Object onEvent(String name, Object obj);
    }

    MuonEventTransport transport(MuonEvent event) {
        List<MuonEventTransport> matching = transports(event);

        if (matching.size() != 1) {
            throw new IllegalStateException("Expected 1 transport to match presend, found " + matching.size());
        }
        return matching.get(0);
    }

    List<MuonEventTransport> transports(MuonEvent event) {
        List<MuonEventTransport> matching = new ArrayList<MuonEventTransport>();

        for(EventFilterChain chain: filterChains) {
            if (chain.canHandle(event)) {
                matching.add(chain.getTransport());
            }
        }
        return matching;
    }

    static MuonEvent resourceEvent(String resource, String verb, Object payload) {
        MuonEvent ev = new MuonEvent(resource, payload);
        ev.addHeader("resource", resource);
        ev.addHeader("verb", verb);
        return ev;
    }
}
