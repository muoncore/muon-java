package io.muoncore.transport.resource;

import io.muoncore.Muon;
import io.muoncore.MuonClient;
import io.muoncore.transport.MuonEventTransport;

public interface MuonResourceTransport extends MuonEventTransport {



    public MuonClient.MuonResult emitForReturn(
            String eventName, MuonResourceEvent event);

    public <T> void listenOnResource(String resource,
                                 String verb,
                                 Class<T> type,
                                 Muon.EventResourceTransportListener<T> listener);

}
