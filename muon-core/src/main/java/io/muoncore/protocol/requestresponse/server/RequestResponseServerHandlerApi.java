package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.RequestMetaData;
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
    default <T> void handleRequest(
            final Predicate<RequestMetaData> request,
            final Handler<T> handler,
            final Class<T> requestType) {
        getRequestResponseHandlers().addHandler(new RequestResponseServerHandler<T, Object>() {
            @Override
            public Predicate<RequestMetaData> getPredicate() {
                return request;
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
