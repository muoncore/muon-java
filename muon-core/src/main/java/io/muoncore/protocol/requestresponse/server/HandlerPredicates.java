package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.RequestMetaData;

import java.util.function.Predicate;

public class HandlerPredicates {

    /**
     * Matches all requests coming into a service.
     *
     * If you use this handler, all requests will be matched.
     *
     * If you want a default, only picked if no others match, then investigate overriding the
     * default Handler.
     *
     */
    public static Predicate<RequestMetaData> all() {
        return msg -> true;
    }

    /**
     * Match requests for a given fixed endpoint.
     *
     * Exact matching
     *
     * @param path The path to match exactly on the request.
     */
    public static Predicate<RequestMetaData> path(String path) {
        return msg -> msg.getUrl().equals(path);
    }
}
