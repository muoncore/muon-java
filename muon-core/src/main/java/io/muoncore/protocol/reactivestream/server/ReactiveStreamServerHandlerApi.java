package io.muoncore.protocol.reactivestream.server;

import io.muoncore.protocol.reactivestream.messages.ReactiveStreamSubscriptionRequest;
import org.reactivestreams.Publisher;

public interface ReactiveStreamServerHandlerApi {

    default <T> void publishSource(String name, PublisherLookup.PublisherType type, Publisher<T> publisher) {
        getPublisherLookup().addPublisher(new PublisherLookup.PublisherRecord(name, type, new ImmediatePublisherGenerator<>(publisher)));
    }

    default <T> void publishGeneratedSource(String name, PublisherLookup.PublisherType type, PublisherGenerator<T> generator) {
        getPublisherLookup().addPublisher(new PublisherLookup.PublisherRecord(name, type, generator));
    }
    PublisherLookup getPublisherLookup();

    interface PublisherGenerator<T> {
        Publisher<T> generatePublisher(ReactiveStreamSubscriptionRequest subscriptionRequest);
    }
}
