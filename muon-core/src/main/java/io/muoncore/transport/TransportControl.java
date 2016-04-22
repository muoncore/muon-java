package io.muoncore.transport;

import io.muoncore.message.MuonMessage;
import org.reactivestreams.Publisher;

import java.util.function.Predicate;

public interface TransportControl {
    Publisher<MuonMessage> tap(Predicate<MuonMessage> msg);
    void shutdown();

}
