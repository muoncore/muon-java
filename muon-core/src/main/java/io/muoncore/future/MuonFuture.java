package io.muoncore.future;

import org.reactivestreams.Publisher;

import java.util.concurrent.Future;

public interface MuonFuture<X> extends Future<X> {

    public Publisher<X> toPublisher();
}
