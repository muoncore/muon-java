package io.muoncore.protocol.rpc.server;


import java.util.List;

public interface RequestResponseHandlers {
    List<RequestResponseServerHandler> getHandlers();
    void addHandler(RequestResponseServerHandler handler);
    RequestResponseServerHandler findHandler(ServerRequest inbound);
}
