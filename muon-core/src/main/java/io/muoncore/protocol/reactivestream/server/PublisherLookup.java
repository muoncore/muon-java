package io.muoncore.protocol.reactivestream.server;

import org.reactivestreams.Publisher;

public interface PublisherLookup {
    Publisher lookupPublisher(String name);
}
