package io.muoncore.protocol.rpc.server;

import java.util.function.Predicate;

public interface HandlerPredicate {
    String resourceString();
    Predicate<ServerRequest> matcher();
}
