package io.muoncore.spring.mapping;

import io.muoncore.crud.OldMuon;
import io.muoncore.crud.MuonService;
import io.muoncore.future.MuonFuture;
import io.muoncore.future.MuonFutures;
import io.muoncore.spring.methodinvocation.MuonResourceMethodInvocation;
import io.muoncore.transport.crud.requestresponse.MuonResourceEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class MuonResourceService {

    @Autowired
    private OldMuon muon;

    public void addQueryMapping(String resource, final MuonResourceMethodInvocation methodInvocation) {
        muon.onQuery(resource, methodInvocation.getDecodedParameterType(), new MuonService.MuonQueryListener() {
            @Override
            public MuonFuture onQuery(MuonResourceEvent queryEvent) {
                return MuonFutures.immediately(methodInvocation.invoke(queryEvent));
            }
        });
    }

}
