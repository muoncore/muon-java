package org.muoncore;

import java.util.List;

public interface MuonResourceTransport extends MuonEventTransport {

    public MuonService.MuonResult emitForReturn(
            String eventName, MuonResourceEvent event);

    public void listenOnResource(String resource,
                                 String verb,
                                 Muon.EventResourceTransportListener listener);

}
