package io.muoncore.transport.client;

import io.muoncore.transport.TransportMessage;
import org.reactivestreams.Publisher;

import java.util.function.Predicate;

public interface TransportMessageDispatcher {
    void dispatch(TransportMessage message);
    Publisher<TransportMessage> observe(Predicate<TransportMessage> filter);
}
