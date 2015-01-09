package org.muoncore.transports;

import org.muoncore.Muon;
import org.muoncore.MuonClient;

public interface MuonBroadcastTransport {

    public MuonClient.MuonResult broadcast(String eventName, MuonMessageEvent event);

    public void listenOnBroadcastEvent(String resource,
                                       Muon.EventMessageTransportListener listener);
}
