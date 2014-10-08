package org.muoncore;

import java.util.List;

public interface MuonEventTransport {

    public MuonService.MuonResult emit(String eventName, MuonEvent event);
    public MuonService.MuonResult emitForReturn(
            String eventName, MuonEvent event);

    public void listenOnEvent(String resource,
                         TransportedMuon.EventTransportListener listener);

    public void listenOnResource(String resource,
                               String verb,
                         TransportedMuon.EventTransportListener listener);

    public List<ServiceDescriptor> discoverServices();
}
