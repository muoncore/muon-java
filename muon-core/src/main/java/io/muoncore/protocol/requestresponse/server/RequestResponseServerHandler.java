package io.muoncore.protocol.requestresponse.server;

import java.lang.reflect.Type;

public interface RequestResponseServerHandler<RequestType, ResponseType> {
    HandlerPredicate getPredicate();
    void handle(RequestWrapper<RequestType> request);
    Type getRequestType();
}
