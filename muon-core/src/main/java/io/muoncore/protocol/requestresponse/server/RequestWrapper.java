package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;

import java.util.HashMap;

public interface RequestWrapper<RequestType> {
    Request<RequestType> getRequest();
    void answer(Response<?> response);
    default void ok(Object payload) {
        answer(new Response<>(200, payload));
    }
    default void notFound(Object payload) {
        answer(new Response<>(404, payload));
    }
    default void notFound() {
        notFound(new HashMap<>());
    }
}
