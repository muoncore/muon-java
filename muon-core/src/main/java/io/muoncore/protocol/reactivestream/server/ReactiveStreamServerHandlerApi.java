package io.muoncore.protocol.reactivestream.server;

import org.reactivestreams.Publisher;

public interface ReactiveStreamServerHandlerApi {
    default <T> void publishSource(String name, PublisherLookup.PublisherType type, Publisher<T> publisher) {
        getPublisherLookup().addPublisher(new PublisherLookup.PublisherRecord(name, type, publisher));
    }
    PublisherLookup getPublisherLookup();
}
