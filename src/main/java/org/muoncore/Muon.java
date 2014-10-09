package org.muoncore;

import org.muoncore.filter.EventFilterChain;
import org.muoncore.transport.AMQPEventTransport;
import org.muoncore.transport.HttpEventTransport;
import org.muoncore.transport.LocalEventTransport;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Muon implements MuonService {

    List<EventFilterChain> filterChains = new ArrayList<EventFilterChain>();
    List<MuonEventTransport> transports = new ArrayList<MuonEventTransport>();

    List<MuonExtension> extensions = new ArrayList<MuonExtension>();

    Dispatcher dispatcher = new Dispatcher();

    String serviceIdentifer;

    public Muon() {
//        setupLocalTransport();
        setupAMQPTransport();
//        setupHttpTransport();
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

    public String getServiceIdentifer() {
        return serviceIdentifer;
    }

    public void setServiceIdentifer(String serviceIdentifer) {
        this.serviceIdentifer = serviceIdentifer;
    }

    @Override
    public void emit(MuonBroadcastEvent ev) {
        dispatcher.dispatchToTransports(ev, transports(ev));
    }

    @Override
    public MuonResult get(String resourceQuery) {
        MuonResourceEvent ev = null;
        try {
            ev = resourceEvent("get", new MuonResourceEvent(new URI(resourceQuery), null, null));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return transport(ev).emitForReturn(resourceQuery, ev);
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

    @Override
    public void receive(String event, final MuonListener listener) {
        for(MuonEventTransport transport: transports) {
            transport.listenOnEvent(event, new EventBroadcastTransportListener() {
                @Override
                public Object onEvent(String name, MuonBroadcastEvent obj) {
                    listener.onEvent(obj);
                    return null;
                }
            });
        }
    }

    @Override
    public void resource(String resource, String descriptor, final MuonGet listener) {
        for(MuonEventTransport transport: transports) {
            transport.listenOnResource(resource, "get", new EventResourceTransportListener() {
                @Override
                public Object onEvent(String name, MuonResourceEvent obj) {
                    return listener.onQuery(obj);
                }
            });
        }
    }

    @Override
    public void resource(String resource, String descriptor, final MuonPost listener) {
        for(MuonEventTransport transport: transports) {
            transport.listenOnResource(resource, "post", new EventResourceTransportListener() {
                @Override
                public Object onEvent(String name, MuonResourceEvent obj) {
                    return listener.onCommand(obj);
                }
            });
        }
    }

    @Override
    public void resource(String resource, String descriptor, final MuonPut listener) {
        for(MuonEventTransport transport: transports) {
            transport.listenOnResource(resource, "put", new EventResourceTransportListener() {
                @Override
                public Object onEvent(String name, MuonResourceEvent obj) {
                    return listener.onCommand(obj);
                }
            });
        }
    }

    @Override
    public void resource(String resource, String descriptor, final MuonDelete listener) {
        for(MuonEventTransport transport: transports) {
            transport.listenOnResource(resource, "delete", new EventResourceTransportListener() {
                @Override
                public Object onEvent(String name, MuonResourceEvent obj) {
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

    public static interface EventBroadcastTransportListener {
        Object onEvent(String name, MuonBroadcastEvent obj);
    }

    public static interface EventResourceTransportListener {
        Object onEvent(String name, MuonResourceEvent obj);
    }

    MuonEventTransport transport(MuonResourceEvent event) {
        //TODO, replace with something that understands resource/ broadcast/ message split

//        List<MuonEventTransport> matching = transports(event);
//
//        if (matching.size() != 1) {
//            throw new IllegalStateException("Expected 1 transport to match presend, found " + matching.size());
//        }
//        return matching.get(0);
        return transports.get(0);
    }

    List<MuonEventTransport> transports(MuonResourceEvent event) {
        List<MuonEventTransport> matching = new ArrayList<MuonEventTransport>();

//        for(EventFilterChain chain: filterChains) {
//            if (chain.canHandle(event)) {
//                matching.add(chain.getTransport());
//            }
//        }
//        return matching;
        return transports;
    }
    List<MuonEventTransport> transports(MuonBroadcastEvent event) {
        List<MuonEventTransport> matching = new ArrayList<MuonEventTransport>();

//        for(EventFilterChain chain: filterChains) {
//            if (chain.canHandle(event)) {
//                matching.add(chain.getTransport());
//            }
//        }
//        return matching;
        return transports;
    }
    static MuonResourceEvent resourceEvent(String verb, MuonResourceEvent payload) {
        payload.addHeader("verb", verb);
        return payload;
    }
}
