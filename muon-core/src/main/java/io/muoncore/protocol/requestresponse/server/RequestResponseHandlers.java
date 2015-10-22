package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.RequestMetaData;

public interface RequestResponseHandlers {
    void addHandler(RequestResponseServerHandler handler);
    RequestResponseServerHandler findHandler(RequestMetaData inbound);
}
