package io.muoncore.protocol.requestresponse.server;

import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;

public interface RequestWrapper<X,Y> {
    Request<X> getRequest();
    void answer(Response<Y> response);
}
