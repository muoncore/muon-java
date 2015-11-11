package io.muoncore.protocol.reactivestream.server;

import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultPublisherLookup implements PublisherLookup {

    private Map<String, Publisher> publishers = new HashMap<>();

    @Override
    public Optional<Publisher> lookupPublisher(String name) {
        return Optional.ofNullable(publishers.get(name));
    }

    @Override
    public void addPublisher(String name, Publisher publisher) {
        publishers.put(name, publisher);
    }
}
