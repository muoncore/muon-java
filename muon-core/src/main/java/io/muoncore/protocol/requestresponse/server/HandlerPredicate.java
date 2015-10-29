package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.RequestMetaData;

import java.util.function.Predicate;

public interface HandlerPredicate {
    String resourceString();
    Predicate<RequestMetaData> matcher();
}
