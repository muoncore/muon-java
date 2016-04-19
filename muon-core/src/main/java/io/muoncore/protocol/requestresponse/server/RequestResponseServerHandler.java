package io.muoncore.protocol.requestresponse.server;

public interface RequestResponseServerHandler {
    HandlerPredicate getPredicate();
    void handle(RequestWrapper request);
}
