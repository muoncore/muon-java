package org.muoncore;

import org.muoncore.filter.EventFilterChain;

import java.util.List;

public class MuonExtensionApi {

    private List<EventFilterChain> filters;
    private List<MuonEventTransport> transports;
    private Muon muon;

    public MuonExtensionApi(Muon muon, List<EventFilterChain> filters, List<MuonEventTransport> transports) {
        this.muon = muon;
        this.filters = filters;
        this.transports = transports;
    }

    public Muon getMuon() {
        return muon;
    }

    public List<EventFilterChain> getFilterChains() {
        return filters;
    }

    public void setFilters(List<EventFilterChain> filters) {
        this.filters = filters;
    }

    public List<MuonEventTransport> getTransports() {
        return transports;
    }

    public void setTransports(List<MuonEventTransport> transports) {
        this.transports = transports;
    }
}
