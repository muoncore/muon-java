package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.Headers;

import java.util.List;

public interface RequestResponseHandlers {
    List<RequestResponseServerHandler> getHandlers();
    void addHandler(RequestResponseServerHandler handler);
    RequestResponseServerHandler findHandler(Headers inbound);
}
