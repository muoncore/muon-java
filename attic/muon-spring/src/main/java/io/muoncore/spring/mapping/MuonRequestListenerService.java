package io.muoncore.spring.mapping;

import io.muoncore.Muon;
import io.muoncore.protocol.rpc.client.requestresponse.server.HandlerPredicate;
import io.muoncore.protocol.rpc.client.requestresponse.server.RequestResponseServerHandlerApi;
import io.muoncore.protocol.rpc.client.requestresponse.server.ServerRequest;
import io.muoncore.protocol.rpc.client.requestresponse.server.ServerResponse;
import io.muoncore.spring.methodinvocation.MuonRequestMethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Predicate;

public class MuonRequestListenerService {

    @Autowired
    private Muon muon;

    public void addRequestMapping(String resource, final MuonRequestMethodInvocation methodInvocation) {
        muon.handleRequest(resourcePredicate(resource), resourceHandler(methodInvocation));
    }

    private RequestResponseServerHandlerApi.Handler resourceHandler(MuonRequestMethodInvocation methodInvocation) {
        return wrapper -> {
            Object result = methodInvocation.invoke(wrapper);
            final ServerResponse response = new ServerResponse(200, result);
            wrapper.answer(response);
        };
    }

    private HandlerPredicate resourcePredicate(String resource) {
        return new HandlerPredicate() {
            @Override
            public String resourceString() {
                return resource;
            }

            @Override
            public Predicate<ServerRequest> matcher() {
                return requestMetaData -> requestMetaData.getUrl().equals(resource);
            }
        };
    }
}
