package com.simplicity.services;

import org.muoncore.MuonService;
import org.muoncore.TransportedMuon;
import org.muoncore.extension.introspection.IntrospectionExtension;

public class Service {

    public static void main(String[] args) {

        MuonService muon = new TransportedMuon();
        muon.registerExtension(new IntrospectionExtension());

        muon.receive("something", new MuonService.MuonListener() {
            @Override
            public void onEvent(Object event) {
                System.out.println("Hello World " + event);
            }
        });

        muon.resource("/mydata", "Get Some Data", new MuonService.MuonGet() {
            @Override
            public Object onQuery(Object queryEvent) {
                return "<h1> This is awesome!!</h1>";
            }
        });
    }
}
