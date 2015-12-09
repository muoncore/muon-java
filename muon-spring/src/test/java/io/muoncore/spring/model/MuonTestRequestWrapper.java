package io.muoncore.spring.model;

import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.protocol.requestresponse.server.RequestWrapper;

/**
 * Created by volod on 11/26/2015.
 */
public class MuonTestRequestWrapper<T> implements RequestWrapper<T> {

    private Request<T> request;
    private Response<?> response;

    public MuonTestRequestWrapper(Request<T> request) {
        this.request = request;
    }

    @Override
    public Request<T> getRequest() {
        return request;
    }

    @Override
    public void answer(Response<?> response) {
        this.response = response;
    }

    public Response<?> getResponse() {
        return response;
    }
}
