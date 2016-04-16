package io.muoncore.transport.client;

import io.muoncore.message.MuonMessage;
import org.reactivestreams.Publisher;

import java.util.function.Predicate;

public interface TransportMessageDispatcher {
    void shutdown();
    void dispatch(MuonMessage message);
    Publisher<MuonMessage> observe(Predicate<MuonMessage> filter);
}
