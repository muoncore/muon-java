package io.muoncore.transport;

import org.reactivestreams.Publisher;

import java.util.function.Predicate;

public interface TransportControl {
    Publisher<TransportMessage> tap(Predicate<TransportMessage> msg);
    void shutdown();
}
