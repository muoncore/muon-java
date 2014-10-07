package org.muoncore;

import java.util.List;

public interface MuonEventTransport {

    public Muon.MuonResult emit(String eventName, MuonEvent event);
    public Muon.MuonResult emitForReturn(
            String eventName, MuonEvent event);

    public void listenOnEvent(String resource,
                         TransportedMuon.EventTransportListener listener);

    public void listenOnResource(String resource,
                               String verb,
                         TransportedMuon.EventTransportListener listener);

//    public List<String> discoverServices();
}
