package io.muoncore.protocol.requestresponse.server;

import java.lang.reflect.Type;

public interface RequestResponseServerHandlerApi extends
        RequestResponseHandlersSource {

    /**
     * RPC handler API. Each incoming request will be passed to the handler instance for it
     * to reply to.
     *
     * The predicate is used to match requests.
     */
    default void handleRequest(
            final HandlerPredicate predicate,
            final Handler handler) {
        getRequestResponseHandlers().addHandler(new RequestResponseServerHandler() {
            @Override
            public HandlerPredicate getPredicate() {
                return predicate;
            }

            @Override
            public void handle(RequestWrapper request) {
                handler.handle(request);
            }
        });
    }

    interface Handler {
        void handle(RequestWrapper wrapper);
    }
}
