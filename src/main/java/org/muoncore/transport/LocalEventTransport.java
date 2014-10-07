package org.muoncore.transport;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.muoncore.Muon;
import org.muoncore.MuonEvent;
import org.muoncore.MuonEventTransport;
import org.muoncore.TransportedMuon;

import java.util.concurrent.CountDownLatch;

public class LocalEventTransport implements MuonEventTransport {

    private EventBus bus;

    public LocalEventTransport() {
        bus = new EventBus();
        System.out.println("LEB: Started");
    }

    @Override
    public Muon.MuonResult emit(String eventName, MuonEvent event) {

        System.out.println("LEB: event " + eventName);

        bus.post(event);
        return new Muon.MuonResult();
    }

    @Override
    public Muon.MuonResult emitForReturn(String eventName, MuonEvent event) {

        final CountDownLatch latch = new CountDownLatch(1);

        final Muon.MuonResult result = new Muon.MuonResult();

        System.out.println("LEB: Sending resource query " + eventName);

        EBResponseListener response = new EBResponseListener() {
            @Subscribe
            @Override
            public void onEvent(EBResponseEvent ev) {
                result.setEvent(ev.getEvent().getPayload());
                System.out.println("LEB: Got " + ev.getEvent().getPayload());
                latch.countDown();
            }
        };

        bus.register(response);
        bus.post(event);
        try {
            //todo, timeout?
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("LEB: Returing resource data " + result.getEvent());
        return result;
    }

    @Override
    public void listenOnEvent(final String resource, final TransportedMuon.EventTransportListener listener) {
        System.out.println("LEB: Listening for event " + resource);
        bus.register(new EBListener() {
            @Override
            @Subscribe
            public void onEvent(MuonEvent ev) {
                System.out.println("LEB: Received for event " + resource);
                if (resource.equals(ev.getResource())) {
                    listener.onEvent(resource, ev.getPayload());
                    //TODO, send the response
                }
            }
        });
    }

    @Override
    public void listenOnResource(final String resource, final String verb, final TransportedMuon.EventTransportListener listener) {
        System.out.println("LEB: Listening for resource " + resource);
        bus.register(new EBListener() {
            @Override
            @Subscribe
            public void onEvent(MuonEvent ev) {
                String verb = ev.getHeaders().get("verb");
                if (resource.equals(ev.getResource()) && verb != null && verb.equals(verb)) {
                    System.out.println("LEB: " + verb + " " + resource + " == ");
                    Object ret = listener.onEvent(resource, ev.getPayload());
                    bus.post(new EBResponseEvent(new MuonEvent(resource, ret)));
                }
            }
        });
    }

    static interface EBListener {
        void onEvent(MuonEvent ev);
    }

    static class EBResponseEvent {
        private MuonEvent event;

        public EBResponseEvent(MuonEvent event) {
            this.event = event;
        }

        public MuonEvent getEvent() {
            return event;
        }
    }

    static interface EBResponseListener {
        void onEvent(EBResponseEvent ev);
    }

}
