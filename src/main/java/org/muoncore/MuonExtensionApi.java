package org.muoncore;

import org.muoncore.filter.EventFilterChain;

import java.util.Collections;
import java.util.List;

/**
 * Passed into library extensions to allow interfering with and inspection of the runtime
 */
public class MuonExtensionApi {

    private List<EventFilterChain> filters;
    private List<MuonEventTransport> transports;
    private Muon muon;
    private Dispatcher dispatcher;
    private List<MuonExtension> extensions;
    private List<MuonResourceRegister> resources;
    private List<MuonEventRegister> events;

    public MuonExtensionApi(
            Muon muon,
            List<EventFilterChain> filters,
            List<MuonEventTransport> transports,
            Dispatcher dispatcher,
            List<MuonExtension> extensions,
            List<MuonEventRegister> events,
            List<MuonResourceRegister> resource) {
        this.muon = muon;
        this.filters = filters;
        this.transports = transports;
        this.dispatcher = dispatcher;
        this.extensions = extensions;
        this.resources = resource;
        this.events = events;
    }

    public List<MuonEventRegister> getEvents() {
        return events;
    }

    public List<MuonResourceRegister> getResources() {
        return resources;
    }

    public List<MuonExtension> getExtensions() {
        return extensions;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public MuonService getMuon() {
        return muon;
    }

    public List<EventFilterChain> getFilterChains() {
        return filters;
    }

    public void setFilters(List<EventFilterChain> filters) {
        this.filters = filters;
    }

    public List<MuonEventTransport> getTransports() {
        return Collections.unmodifiableList(transports);
    }

    public void addTransport(MuonEventTransport transport) {
        muon.registerTransport(transport);
    }

    void setTransports(List<MuonEventTransport> transports) {
        this.transports = transports;
    }
}



