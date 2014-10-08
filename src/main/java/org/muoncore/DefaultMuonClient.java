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

public class DefaultMuonClient implements MuonClient {

    List<EventFilterChain> filterChains = new ArrayList<EventFilterChain>();
    List<MuonEventTransport> transports = new ArrayList<MuonEventTransport>();

    List<MuonExtension> extensions = new ArrayList<MuonExtension>();

    Dispatcher dispatcher = new Dispatcher();

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
