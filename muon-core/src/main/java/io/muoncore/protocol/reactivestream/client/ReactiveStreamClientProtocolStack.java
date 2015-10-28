package io.muoncore.protocol.reactivestream.client;

import org.reactivestreams.Publisher;

public interface ReactiveStreamClientProtocolStack {

    default <R> Publisher<R> lookupStream(String uri, Class<R> eventType) {
        return null;
    }
}
