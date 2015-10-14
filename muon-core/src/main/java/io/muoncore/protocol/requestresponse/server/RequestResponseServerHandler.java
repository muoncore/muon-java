package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.Request;

import java.util.function.Predicate;

public interface RequestResponseServerHandler {
    Predicate<Request> getPredicate();
    void handle(RequestWrapper request);
}
