package org.muoncore.transports;

import org.muoncore.Muon;
import org.muoncore.MuonClient;

public interface MuonQueueTransport  extends MuonEventTransport {

    public MuonClient.MuonResult send(String queueName, MuonMessageEvent event);

    public void listenOnQueueEvent(String queueName,
                                       Muon.EventMessageTransportListener listener);
}
