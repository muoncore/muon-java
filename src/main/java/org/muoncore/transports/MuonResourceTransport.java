package org.muoncore.transports;

import org.muoncore.Muon;
import org.muoncore.MuonClient;
import org.muoncore.MuonEventTransport;

public interface MuonResourceTransport extends MuonEventTransport {

    public MuonClient.MuonResult emitForReturn(
            String eventName, MuonResourceEvent event);

    public void listenOnResource(String resource,
                                 String verb,
                                 Muon.EventResourceTransportListener listener);

}
