package io.muoncore.spring;

import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.RequestMetaData;
import io.muoncore.protocol.requestresponse.server.RequestWrapper;
import io.muoncore.spring.model.MuonTestRequestWrapper;

import java.net.URISyntaxException;

public class MuonTestUtils {
    public static <T> RequestWrapper<T> getSampleMuonRequestWrapper(T payload) throws URISyntaxException {
        final RequestMetaData metaData = new RequestMetaData("muon://service/resource", "sourceService", "targetService");
        final Request<T> request = new Request<>(metaData, payload);
        return new MuonTestRequestWrapper<>(request);
    }
}
