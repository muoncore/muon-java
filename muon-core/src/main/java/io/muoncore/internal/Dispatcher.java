package io.muoncore.internal;

import io.muoncore.codec.Codecs;
import io.muoncore.codec.TransportCodecType;
import io.muoncore.transport.broadcast.MuonBroadcastTransport;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.resource.MuonResourceEvent;

import java.util.ArrayList;
import java.util.List;

public class Dispatcher {

    private List<Listener> listeners = new ArrayList<Listener>();
    private Codecs codecs;

    public Dispatcher(Codecs codecs) {
        this.codecs = codecs;
    }

    public void dispatchToTransports(MuonMessageEvent event, List<MuonBroadcastTransport> transports) {
        for(Listener listener: listeners) {
            listener.presend(event);
        }
        for (MuonBroadcastTransport transport: transports) {
            encode(event, transport.getCodecType());
            transport.broadcast(event.getEventName(), event);
        }
    }

    private <T> void encode(MuonMessageEvent<T> ev, TransportCodecType type) {
        if (ev.getDecodedContent() != null) {
            if (type == TransportCodecType.BINARY) {
                byte[] content = codecs.encodeToByte(ev.getDecodedContent());
                ev.setEncodedBinaryContent(content);
            } else {
                String content = codecs.encodeToString(ev.getDecodedContent());
                ev.setEncodedStringContent(content);
            }
        }
    }


    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public static interface Listener {
        void presend(MuonMessageEvent event);
    }
}
