package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.transport.TransportClientSource;
import org.reactivestreams.Publisher;

import java.util.function.Predicate;

public interface RequestResponseServerHandlerApi extends
        RequestResponseHandlersSource {

    /**
     * Simple handler API. Each incoming request will be passed to the handler instance for it
     * to reply to.
     *
     * The predicate is used to match requests.
     */
    default void handleRequest(
            final Predicate<Request> request,
            final Handler handler) {
        getRequestResponseHandlers().addHandler(new RequestResponseServerHandler() {
            @Override
            public Predicate<Request> getPredicate() {
                return request;
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
