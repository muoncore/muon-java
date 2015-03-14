package io.muoncore.transports;

import io.muoncore.Muon;
import io.muoncore.MuonClient;

public interface MuonQueueTransport  extends MuonEventTransport {

    public <T> MuonClient.MuonResult send(String queueName, MuonMessageEvent<T> event);

    public <T> void listenOnQueueEvent(String queueName,
                                    Class<T> messageType,
                                       Muon.EventMessageTransportListener listener);
}
