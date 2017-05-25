package io.muoncore.spring.model;

import io.muoncore.protocol.rpc.client.requestresponse.server.RequestWrapper;
import io.muoncore.protocol.rpc.client.requestresponse.server.ServerRequest;
import io.muoncore.protocol.rpc.client.requestresponse.server.ServerResponse;


/**
 * Created by volod on 11/26/2015.
 */
public class MuonTestRequestWrapper implements RequestWrapper {

    private ServerRequest request;
    private ServerResponse response;

    public MuonTestRequestWrapper(ServerRequest request) {
        this.request = request;
    }

    @Override
    public ServerRequest getRequest() {
        return request;
    }

    @Override
    public void answer(ServerResponse response) {
        this.response = response;
    }

    public ServerResponse getResponse() {
        return response;
    }
}
