package org.muoncore;

public interface MuonBroadcastTransport {

    public MuonService.MuonResult emit(String eventName, MuonBroadcastEvent event);

    public void listenOnEvent(String resource,
                              Muon.EventBroadcastTransportListener listener);
}
