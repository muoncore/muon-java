package org.muoncore.filter;

import org.muoncore.transports.MuonMessageEvent;
import org.muoncore.MuonEventTransport;

import java.util.ArrayList;
import java.util.List;

public class EventFilterChain {
    private MuonEventTransport transport;
    private List<EventFilter> chain = new ArrayList<EventFilter>();

    public EventFilterChain(MuonEventTransport transport) {
        this.transport = transport;
    }

    public boolean canHandle(MuonMessageEvent event) {
        boolean canHandle = true;

        for(EventFilter filter: chain) {
            canHandle = filter.canHandle(event);
            if (!canHandle) break;
        }

        return canHandle;
    }

    public void addFilter(String name, EventFilter filter) {
        chain.add(filter);
    }

    public void removeFilter(String filter) {
        ///TODO, add some way of exposing and managing the chain via events.
        throw new IllegalStateException("Not Implemented");
    }

    public MuonEventTransport getTransport() {
        return transport;
    }
}
