package io.muoncore.protocol.requestresponse.server;

import java.util.function.Predicate;

public interface HandlerPredicate {
    String resourceString();
    Predicate<ServerRequest> matcher();
}
