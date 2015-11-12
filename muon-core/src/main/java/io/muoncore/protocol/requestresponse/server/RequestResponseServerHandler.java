package io.muoncore.protocol.requestresponse.server;

public interface RequestResponseServerHandler<RequestType, ResponseType> {
    HandlerPredicate getPredicate();
    void handle(RequestWrapper<RequestType> request);
    Class<RequestType> getRequestType();
}
