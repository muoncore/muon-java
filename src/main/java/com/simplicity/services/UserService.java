package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.MuonBroadcastEvent;
import org.muoncore.MuonResourceEvent;
import org.muoncore.MuonService;
import org.muoncore.extension.eventlogger.EventLoggerExtension;
import org.muoncore.extension.introspection.IntrospectionExtension;

public class UserService {

    public static void main(String[] args) {

        MuonService muon = new Muon();

        muon.setServiceIdentifer("users");

        muon.resource("mydata/happy", "Get Some Data", new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {
                return "<h1>Got some user data!</h1>";
            }
        });
    }
}
