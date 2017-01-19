package io.muoncore.spring.repository;

import org.springframework.stereotype.Repository;

@Repository
public class DefaultMuonEventStoreRepository implements MuonEventStoreRepository {

//    @Autowired
//    private Muon muon;
//
//    @Override
//    public void replay(String streamName, EventReplayMode mode, Subscriber<Event> event) throws UnsupportedEncodingException, URISyntaxException {
//        muon.getEventStoreClient().replay(streamName, mode, event);
//    }
//
//    @Override
//    public void event(String eventType, String streamName, Object payload) {
//        event(eventType, streamName, "", payload);
//    }
//
//    @Override
//    public void event(String eventType, Object payload) {
//        event(eventType, "general", payload);
//    }
//
//    @Override
//    public void event(String eventType, String streamName, String parentId, Object payload) {
//        muon.getEventStoreClient().event(
//                new Event<>(
//                        eventType,
//                        UUID.randomUUID().toString(),
//                        parentId,
//                        muon.getConfiguration().getServiceName(),
//                        payload));
//    }
}
