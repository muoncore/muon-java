package io.muoncore.protocol.requestresponse.server;

import java.lang.reflect.Type;

public interface RequestResponseServerHandlerApi extends
        RequestResponseHandlersSource {

    /**
     * Simple handler API. Each incoming request will be passed to the handler instance for it
     * to reply to.
     *
     * The predicate is used to match requests.
     */
    default <T> void handleRequest(
            final HandlerPredicate predicate,
            final Class<T> requestType,
            final Handler<T> handler) {
        handleRequest(predicate, (Type) requestType, handler);
    }

    default <T> void handleRequest(
            final HandlerPredicate predicate,
            final Type requestType,
            final Handler<T> handler) {
        getRequestResponseHandlers().addHandler(new RequestResponseServerHandler<T, Object>() {
            @Override
            public HandlerPredicate getPredicate() {
                return predicate;
            }

            @Override
            public void handle(RequestWrapper<T> request) {
                handler.handle(request);
            }

            @Override
            public Type getRequestType() {
                return requestType;
            }
        });
    }

    interface Handler<RequestType> {
        void handle(RequestWrapper<RequestType> wrapper);
    }
}
