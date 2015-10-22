package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.RequestMetaData;

import java.util.function.Predicate;

public interface RequestResponseServerHandler<RequestType, ResponseType> {
    Predicate<RequestMetaData> getPredicate();
    void handle(RequestWrapper<RequestType, ResponseType> request);
    Class<RequestType> getRequestType();
}
