package io.muoncore;

import io.muoncore.internal.ImmediateReturnFuture;
import io.muoncore.internal.PublisherBackedFuture;
import org.reactivestreams.Publisher;

public class MuonFutures {

    public static <X> MuonFuture<X> immediately(X ret) {
        return new ImmediateReturnFuture<X>(ret);
    }

    public static <X> MuonFuture<X> fromPublisher(Publisher<X> publisher) {
        return new PublisherBackedFuture<X>(publisher);
    }
}
