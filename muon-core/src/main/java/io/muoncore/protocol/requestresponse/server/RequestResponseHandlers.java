package io.muoncore.protocol.requestresponse.server;


import java.util.List;

public interface RequestResponseHandlers {
    List<RequestResponseServerHandler> getHandlers();
    void addHandler(RequestResponseServerHandler handler);
    RequestResponseServerHandler findHandler(ServerRequest inbound);
}
