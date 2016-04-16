package io.muoncore.protocol.reactivestream.server;

import io.muoncore.protocol.reactivestream.messages.ReactiveStreamSubscriptionRequest;
import org.reactivestreams.Publisher;

public class ImmediatePublisherGenerator<T> implements ReactiveStreamServerHandlerApi.PublisherGenerator<T> {
    private Publisher<T> publisher;

    public ImmediatePublisherGenerator(Publisher<T> publisher) {
        this.publisher = publisher;
    }

    @Override
    public Publisher<T> generatePublisher(ReactiveStreamSubscriptionRequest subscriptionRequest) {
        return publisher;
    }
}
