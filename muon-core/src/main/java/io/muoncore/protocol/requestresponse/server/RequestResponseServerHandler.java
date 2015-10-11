package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.transport.TransportClientSource;
import org.reactivestreams.Publisher;

import java.util.function.Predicate;

public interface RequestResponseServerHandler extends
        TransportClientSource {

    default <X, Y> Publisher<RequestWrapper<X,Y>> handleRequest(Predicate<Request<X>> request) {

        return null;

    }
}
