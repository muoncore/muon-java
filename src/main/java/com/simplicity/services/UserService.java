package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.transports.MuonResourceEvent;
import org.muoncore.MuonService;
import org.muoncore.extension.amqp.AmqpTransportExtension;

public class UserService {

    public static void main(String[] args) {

        MuonService muon = new Muon();
        muon.registerExtension(new AmqpTransportExtension());

        muon.setServiceIdentifer("users");

        muon.onGet("mydata/happy", "Get Some Data", new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {
                return "<h1>Got some user data!</h1>";
            }
        });
        muon.start();
    }
}
