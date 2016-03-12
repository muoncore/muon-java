package io.muoncore.spring.repository;

import io.muoncore.Muon;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.client.EventReplayMode;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.UUID;

@Repository
public class DefaultMuonEventStoreRepository implements MuonEventStoreRepository {

    @Autowired
    private Muon muon;

    @Override
    public void replay(String streamName, EventReplayMode mode, Subscriber<Event> event) throws UnsupportedEncodingException, URISyntaxException {
        muon.getEventStoreClient().replay(streamName, mode, event);
    }

    @Override
    public void event(String eventType, String streamName, Object payload) {
        event(eventType, streamName, "", payload);
    }

    @Override
    public void event(String eventType, Object payload) {
        event(eventType, "general", payload);
    }

    @Override
    public void event(String eventType, String streamName, String parentId, Object payload) {
        muon.getEventStoreClient().event(
                new Event<>(
                        eventType,
                        UUID.randomUUID().toString(),
                        parentId,
                        muon.getConfiguration().getServiceName(),
                        payload));
    }
}
