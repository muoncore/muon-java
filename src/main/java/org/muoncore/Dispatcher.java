package org.muoncore;

import java.util.ArrayList;
import java.util.List;

public class Dispatcher {

    private List<Listener> listeners = new ArrayList<Listener>();


    void dispatchToTransports(MuonEvent event, List<MuonEventTransport> transports) {
        for(Listener listener: listeners) {
            listener.presend(event);
        }
        for (MuonEventTransport transport: transports) {
            transport.emit(event.getResource(), event);
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public static interface Listener {
        void presend(MuonEvent event);
    }
}
