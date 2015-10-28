package io.muoncore.protocol.reactivestream.server;

import org.reactivestreams.Publisher;

public interface ReactiveStreamServerHandlerApi {

    default <T> void publish(Publisher<T> publisher) {

    }
}
