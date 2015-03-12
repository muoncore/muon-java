package org.muoncore.extension.local;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.muoncore.*;
import org.muoncore.codec.TransportCodecType;
import org.muoncore.transports.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class LocalEventTransport implements MuonResourceTransport,MuonBroadcastTransport {

    private Logger log = Logger.getLogger(LocalEventTransport.class.getName());
    private EventBus bus;

    public LocalEventTransport() {
        bus = new EventBus();
        log.info("Local Event Bus Started");
    }

    @Override
    public MuonService.MuonResult broadcast(String eventName, MuonMessageEvent event) {

        log.fine("Listening for event " + eventName);

        bus.post(event);
        return new MuonService.MuonResult();
    }

    @Override
    public MuonService.MuonResult emitForReturn(String eventName, final MuonResourceEvent event) {

        final CountDownLatch latch = new CountDownLatch(1);

        final MuonService.MuonResult result = new MuonService.MuonResult();

        log.finer("Sending onGet query " + eventName);

        EBResponseListener response = new EBResponseListener() {
            @Subscribe
            @Override
            public void onEvent(EBResponseEvent ev) {
                result.setEvent(ev.getEvent());
                latch.countDown();
            }
        };

        bus.register(response);
        bus.post(event);
        try {
            //todo, timeout? allow shutting down?
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.finer("LEB: Returing onGet data " + result.getResponseEvent());
        return result;
    }

    @Override
    public void listenOnBroadcastEvent(final String resource, final Muon.EventMessageTransportListener listener) {
        log.fine("Listening for event " + resource);
        bus.register(new EBListener() {
            @Override
            @Subscribe
            public void onEvent(MuonMessageEvent ev) {
                log.finer("Received for event " + resource);
                if (resource.equals(ev.getEventName())) {
                    listener.onEvent(resource, ev);
                }
            }
        });
    }

    @Override
    public <T> void listenOnResource(final String resource, String verb, Class<T> type, final Muon.EventResourceTransportListener<T> listener) {
        log.fine("Listening for onGet " + resource);
        bus.register(new EBResourceListener() {
            @Override
            @Subscribe
            public void onEvent(MuonResourceEvent ev) {
                String verb = (String) ev.getHeaders().get("verb");

                if (resource.equals(ev.getServiceId()) && verb != null && verb.equals(verb)) {
                    log.finer(verb + " " + resource + " == ");

                    Object ret = listener.onEvent(resource, ev);

                    bus.post(new EBResponseEvent(
                            MuonResourceEventBuilder
                                    .event((String) ret)
                                    .withUri(ev.getUri().toASCIIString())
                                    .build()));
                }
            }
        });
    }

    public void start() {
        //TODO ....
    }

    @Override
    public void shutdown() { }

    static interface EBListener {
        void onEvent(MuonMessageEvent ev);
    }
    static interface EBResourceListener {
        void onEvent(MuonResourceEvent ev);
    }

    static class EBResponseEvent {
        private MuonResourceEvent event;

        public EBResponseEvent(MuonResourceEvent event) {
            this.event = event;
        }

        public MuonResourceEvent getEvent() {
            return event;
        }
    }

    static interface EBResponseListener {
        void onEvent(EBResponseEvent ev);
    }

    @Override
    public String getUrlScheme() {
        return "local";
    }

    @Override
    public URI getLocalConnectionURI() throws URISyntaxException {
        return new URI("local://local");
    }

    @Override
    public TransportCodecType getCodecType() {
        return TransportCodecType.BINARY;
    }
}
