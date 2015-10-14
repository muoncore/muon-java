package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.Request;

import java.util.ArrayList;
import java.util.List;

public class DynamicRequestResponseHandlers implements RequestResponseHandlers {

    private List<RequestResponseServerHandler> handlers = new ArrayList<>();

    private RequestResponseServerHandler defaultHandler;

    public DynamicRequestResponseHandlers(RequestResponseServerHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    @Override
    public void addHandler(RequestResponseServerHandler handler) {
        handlers.add(handler);
    }

    @Override
    public RequestResponseServerHandler findHandler(Request inbound) {
        assert inbound != null;
        return handlers.stream().filter( handler -> {
            return handler.getPredicate().test(inbound);
        }).findFirst().orElse(defaultHandler);
    }
}
