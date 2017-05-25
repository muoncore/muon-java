package io.muoncore.protocol.rpc.server;

import java.util.HashMap;

public interface RequestWrapper {
    ServerRequest getRequest();
    void answer(ServerResponse response);
    default void ok(Object payload) {
        answer(new ServerResponse(200, payload));
    }
    default void notFound(Object payload) {
        answer(new ServerResponse(404, payload));
    }
    default void notFound() {
        notFound(new HashMap<>());
    }
}
