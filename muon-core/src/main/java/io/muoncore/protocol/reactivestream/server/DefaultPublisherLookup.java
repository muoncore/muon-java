package io.muoncore.protocol.reactivestream.server;

import java.util.*;

public class DefaultPublisherLookup implements PublisherLookup {

    private Map<String, PublisherRecord> publishers = new HashMap<>();

    @Override
    public Optional<PublisherRecord> lookupPublisher(String name) {
        return Optional.ofNullable(publishers.get(name));
    }

    @Override
    public List<PublisherRecord> getPublishers() {
        return new ArrayList<>(publishers.values());
    }

    @Override
    public void addPublisher(PublisherRecord publisherRecord) {
        String name = publisherRecord.getName();
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        publishers.put(name, publisherRecord);
    }
}
