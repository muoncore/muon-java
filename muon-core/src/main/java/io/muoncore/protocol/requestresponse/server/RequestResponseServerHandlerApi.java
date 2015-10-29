package io.muoncore.protocol.requestresponse.server;

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
        getRequestResponseHandlers().addHandler(new RequestResponseServerHandler<T, Object>() {
            @Override
            public HandlerPredicate getPredicate() {
                return predicate;
            }

            @Override
            public void handle(RequestWrapper<T, Object> request) {
                handler.handle(request);
            }

            @Override
            public Class getRequestType() {
                return requestType;
            }
        });
    }

    interface Handler<RequestType> {
        void handle(RequestWrapper<RequestType, ?> wrapper);
    }
}
