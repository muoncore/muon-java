package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.Headers;

import java.util.function.Predicate;

public interface HandlerPredicate {
    String resourceString();
    Predicate<Headers> matcher();
}
