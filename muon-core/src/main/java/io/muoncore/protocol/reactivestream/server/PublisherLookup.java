package io.muoncore.protocol.reactivestream.server;

import org.reactivestreams.Publisher;

import java.util.Optional;

public interface PublisherLookup {
    Optional<Publisher> lookupPublisher(String name);
    void addPublisher(String name, Publisher publisher);
}
