package org.muoncore;

import org.muoncore.extension.local.LocalTransportExtension;
import org.muoncore.filter.EventFilterChain;
import org.muoncore.extension.amqp.AMQPEventTransport;
import org.muoncore.extension.http.HttpEventTransport;
import org.muoncore.extension.local.LocalEventTransport;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**

 TODO, have a muon protocol checked that introspects a remote muon and asserts it
 reports the correct resources and events for a particular protocol

 * TODO
 * Need to consider correct lifecycle for the library.
 *
 *Startup/ shutdown, when to initialise extensions and the like.
 *How/ When to add transports and start them.
 *
 *Create an abstraction layer for the onGet/ event registrations.
 * so events are regsitered here, then sent into the transports at the appropriate time
 * / when they are started, not immediately.
 * That allows transports to be added and removed. Possibly as extensions?
 */
public class Muon implements MuonService {

    private List<EventFilterChain> filterChains = new ArrayList<EventFilterChain>();
    private List<MuonEventTransport> transports = new ArrayList<MuonEventTransport>();
    private List<MuonEventTransport> nonInitTransports = new ArrayList<MuonEventTransport>();

    private List<MuonExtension> extensions = new ArrayList<MuonExtension>();

    private List<MuonResourceRegister> resources = new ArrayList<MuonResourceRegister>();
    private List<MuonEventRegister> events = new ArrayList<MuonEventRegister>();

    private Dispatcher dispatcher = new Dispatcher();

    private String serviceIdentifer;

    private boolean started = false;

    @Override
    public void registerExtension(MuonExtension extension) {
        extensions.add(extension);
    }

    public Muon() {
        registerExtension(new LocalTransportExtension());
    }

    public void start() {
        for (MuonExtension extension: extensions) {
            extension.init(
                    new MuonExtensionApi(
                            this,
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
        started = true;
    }

    void registerTransport(MuonEventTransport transport) {
        transports.add(transport);

        if (!started) {
            nonInitTransports.add(transport);
        } else {
            //post start addition of a transport, maybe not ideal, but certainly not prevented either.
            //permits the addition of new transports dynamically, which may be useful.
            initialiseTransport(transport);
        }
    }

    private void initialiseTransport(MuonEventTransport transport) {
        transport.start();

        System.out.println("Muon: Starting transport " + transport.getClass().getSimpleName());
        //TODO, add resources

        //TODO, add events

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
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
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
    public void onGet(String resource, String descriptor, final MuonGet listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
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
    public void onPost(String resource, String descriptor, final MuonPost listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
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
    public void onPut(String resource, String descriptor, final MuonPut listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
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
    public void onDelete(String resource, String descriptor, final MuonDelete listener) {
        //TODO, extract this into some lifecycle init during start.
        //instead just store this.
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
        //TODO, replace with something that understands onGet/ broadcast/ message split

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
