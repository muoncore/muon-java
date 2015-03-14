package io.muoncore.transports;

import io.muoncore.Muon;
import io.muoncore.MuonClient;

public interface MuonBroadcastTransport extends MuonEventTransport {

    public MuonClient.MuonResult broadcast(String eventName, MuonMessageEvent event);

    public void listenOnBroadcastEvent(String resource,
                                       Muon.EventMessageTransportListener listener);
}
