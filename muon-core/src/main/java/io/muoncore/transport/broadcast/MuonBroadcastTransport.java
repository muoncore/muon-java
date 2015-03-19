package io.muoncore.transport.broadcast;

import io.muoncore.Muon;
import io.muoncore.MuonClient;
import io.muoncore.transport.MuonEventTransport;
import io.muoncore.transport.MuonMessageEvent;

public interface MuonBroadcastTransport extends MuonEventTransport {

    public MuonClient.MuonResult broadcast(String eventName, MuonMessageEvent event);

    public void listenOnBroadcastEvent(String resource,
                                       Muon.EventMessageTransportListener listener);
}
