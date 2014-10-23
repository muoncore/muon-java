package org.muoncore.extension.local;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.muoncore.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LocalEventTransport implements MuonEventTransport {

    private EventBus bus;

    public LocalEventTransport() {
        bus = new EventBus();
        System.out.println("LEB: Started");
    }

    @Override
    public MuonService.MuonResult emit(String eventName, MuonBroadcastEvent event) {

        System.out.println("LEB: event " + eventName);

        bus.post(event);
        return new MuonService.MuonResult();
    }

    @Override
    public MuonService.MuonResult emitForReturn(String eventName, final MuonResourceEvent event) {

        final CountDownLatch latch = new CountDownLatch(1);

        final MuonService.MuonResult result = new MuonService.MuonResult();

        System.out.println("LEB: Sending onGet query " + eventName);

        EBResponseListener response = new EBResponseListener() {
            @Subscribe
            @Override
            public void onEvent(EBResponseEvent ev) {
                result.setEvent(ev.getEvent());
                System.out.println("LEB: Got " + ev.getEvent().getPayload());
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
        System.out.println("LEB: Returing onGet data " + result.getResponseEvent());
        return result;
    }

    @Override
    public void listenOnEvent(final String resource, final Muon.EventBroadcastTransportListener listener) {
        System.out.println("LEB: Listening for event " + resource);
        bus.register(new EBListener() {
            @Override
            @Subscribe
            public void onEvent(MuonBroadcastEvent ev) {
                System.out.println("LEB: Received for event " + resource);
                if (resource.equals(ev.getEventName())) {
                    listener.onEvent(resource, ev);
                }
            }
        });
    }

    @Override
    public void listenOnResource(final String resource, final String verb, final Muon.EventResourceTransportListener listener) {
        System.out.println("LEB: Listening for onGet " + resource);
        bus.register(new EBResourceListener() {
            @Override
            @Subscribe
            public void onEvent(MuonResourceEvent ev) {
                String verb = ev.getHeaders().get("verb");

                if (resource.equals(ev.getServiceId()) && verb != null && verb.equals(verb)) {
                    System.out.println("LEB: " + verb + " " + resource + " == ");

                    Object ret = listener.onEvent(resource, ev);

                    bus.post(new EBResponseEvent(
                            MuonResourceEventBuilder
                                    .textMessage((String) ret)
                                    .withUri(ev.getUri().toASCIIString())
                                    .build()));
                }
            }
        });
    }

    @Override
    public List<ServiceDescriptor> discoverServices() {
        return Collections.singletonList(new ServiceDescriptor("localhost", this));
    }

    public void start() {
        //TODO ....
    }

    @Override
    public void shutdown() { }

    static interface EBListener {
        void onEvent(MuonBroadcastEvent ev);
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

}
