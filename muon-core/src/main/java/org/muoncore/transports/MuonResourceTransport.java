package org.muoncore.transports;

import org.muoncore.Muon;
import org.muoncore.MuonClient;

public interface MuonResourceTransport extends MuonEventTransport {



    public MuonClient.MuonResult emitForReturn(
            String eventName, MuonResourceEvent event);

    public <T> void listenOnResource(String resource,
                                 String verb,
                                 Class<T> type,
                                 Muon.EventResourceTransportListener<T> listener);

}
