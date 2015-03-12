package org.muoncore.internal;

import org.muoncore.transports.MuonBroadcastTransport;
import org.muoncore.transports.MuonMessageEvent;

import java.util.ArrayList;
import java.util.List;

public class Dispatcher {

    private List<Listener> listeners = new ArrayList<Listener>();


    public void dispatchToTransports(MuonMessageEvent event, List<MuonBroadcastTransport> transports) {
        for(Listener listener: listeners) {
            listener.presend(event);
        }
        for (MuonBroadcastTransport transport: transports) {
            //TODO !!! encode(ev, trans.getCodecType());
            transport.broadcast(event.getEventName(), event);
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public static interface Listener {
        void presend(MuonMessageEvent event);
    }
}
