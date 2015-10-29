package io.muoncore.protocol.reactivestream.client;

import org.reactivestreams.Publisher;

import java.net.URI;

public interface ReactiveStreamClientProtocolStack {

    default <R> Publisher<R> lookupStream(URI uri, Class<R> eventType) {
        return null;
    }
}
