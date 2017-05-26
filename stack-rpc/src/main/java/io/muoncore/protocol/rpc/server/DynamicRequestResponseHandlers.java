package io.muoncore.protocol.rpc.server;

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
    public RequestResponseServerHandler findHandler(ServerRequest inbound) {
        assert inbound != null;
        return handlers.stream().filter( handler -> {
            return handler.getPredicate().matcher().test(inbound);
        }).findFirst().orElse(defaultHandler);
    }

    @Override
    public List<RequestResponseServerHandler> getHandlers() {
        return handlers;
    }
}
