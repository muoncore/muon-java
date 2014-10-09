package org.muoncore;

import java.util.List;

public interface MuonEventTransport {

    public MuonService.MuonResult emit(String eventName, MuonBroadcastEvent event);
    public MuonService.MuonResult emitForReturn(
            String eventName, MuonResourceEvent event);

    public void listenOnEvent(String resource,
                         Muon.EventBroadcastTransportListener listener);

    public void listenOnResource(String resource,
                               String verb,
                         Muon.EventResourceTransportListener listener);

    public List<ServiceDescriptor> discoverServices();

    public void shutdown();
}
