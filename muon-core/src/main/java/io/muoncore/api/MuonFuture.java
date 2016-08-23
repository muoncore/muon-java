package io.muoncore.api;

import org.reactivestreams.Publisher;

import java.util.concurrent.Future;

/**
 * An extended interface over a remote task that will return some data.
 */
public interface MuonFuture<X> extends Future<X> {

    /**
     * Adapt this MuonFuture to use a reactive stream Publisher interface.
     * At most, the returned Publisher will deliver a single item and then complete.
     */
    Publisher<X> toPublisher();

    /**
     * A Promise style API for this MuonFuture.
     *
     * The supplied function will be executed asynchronously when the requested data becomes
     * available after the remote operation is complete.
     *
     */
    void then(PromiseFunction<X> onFulfilled);

}
