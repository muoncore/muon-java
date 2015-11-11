package io.muoncore.protocol.reactivestream.server;

import org.reactivestreams.Publisher;

public interface ReactiveStreamServerHandlerApi {
    default <T> void publishSource(String name, Publisher<T> publisher) {

    }
    PublisherLookup getPublisherLookup();
}
