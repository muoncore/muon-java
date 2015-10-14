package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;

public interface RequestWrapper<RequestType, ResponseType> {
    Request<RequestType> getRequest();
    void answer(Response<ResponseType> response);
}
