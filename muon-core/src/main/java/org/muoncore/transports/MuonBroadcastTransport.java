package org.muoncore.transports;

import org.muoncore.Muon;
import org.muoncore.MuonClient;

public interface MuonBroadcastTransport extends MuonEventTransport {

    public MuonClient.MuonResult broadcast(String eventName, MuonMessageEvent event);

    public void listenOnBroadcastEvent(String resource,
                                       Muon.EventMessageTransportListener listener);
}
