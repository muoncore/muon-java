package io.muoncore.protocol.reactivestream.server;

import org.reactivestreams.Publisher;

public interface ReactiveStreamServerHandlerApi {

    /**
     * High level, simple API.
     *
     * Make the Publisher available for remote subscriptions.
     */
    default <T> void publishSource(String name, Publisher<T> publisher) {

    }

    /**
     * Low level API.
     *
     * provide hooks into the ReactiveStreamServerChannel to allow extensions and overriding of how the
     * messages are interpreted.
     *
     * Specifically, should allow the generation of Publishers on demand.
     */
    default <T> void handleSubscriptionRequest() {

    }
}

