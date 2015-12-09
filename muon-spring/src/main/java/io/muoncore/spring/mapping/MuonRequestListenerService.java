package io.muoncore.spring.mapping;

import io.muoncore.Muon;
import io.muoncore.protocol.requestresponse.RequestMetaData;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.protocol.requestresponse.server.HandlerPredicate;
import io.muoncore.protocol.requestresponse.server.RequestResponseServerHandlerApi;
import io.muoncore.spring.methodinvocation.MuonRequestMethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Predicate;

public class MuonRequestListenerService {

    @Autowired
    private Muon muon;

    public void addRequestMapping(String resource, final MuonRequestMethodInvocation methodInvocation) {
        muon.handleRequest(resourcePredicate(resource), methodInvocation.getDecodedParameterType(), resourceHandler(methodInvocation));
    }

    private RequestResponseServerHandlerApi.Handler resourceHandler(MuonRequestMethodInvocation methodInvocation) {
        return wrapper -> {
            Object result = methodInvocation.invoke(wrapper);
            final Response response = new Response(200, result);
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
            public Predicate<RequestMetaData> matcher() {
                return requestMetaData -> requestMetaData.getUrl().equals(resource);
            }
        };
    }
}
