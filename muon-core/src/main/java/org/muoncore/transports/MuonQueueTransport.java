package org.muoncore.transports;

import org.muoncore.Muon;
import org.muoncore.MuonClient;

public interface MuonQueueTransport  extends MuonEventTransport {

    public <T> MuonClient.MuonResult send(String queueName, MuonMessageEvent<T> event);

    public <T> void listenOnQueueEvent(String queueName,
                                    Class<T> messageType,
                                       Muon.EventMessageTransportListener listener);
}
