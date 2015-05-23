package io.muoncore.future;

import org.reactivestreams.Publisher;

import java.util.concurrent.Future;

public class MuonFutures {

    public static <X> MuonFuture<X> immediately(X ret) {
        return new ImmediateReturnFuture<X>(ret);
    }

    public static <X> MuonFuture<X> fromPublisher(Publisher<X> publisher) {
        return new PublisherBackedFuture<X>(publisher);
    }
}
