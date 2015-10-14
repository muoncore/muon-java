package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.Request;

public interface RequestResponseHandlers {
    void addHandler(RequestResponseServerHandler handler);
    RequestResponseServerHandler findHandler(Request inbound);
}
