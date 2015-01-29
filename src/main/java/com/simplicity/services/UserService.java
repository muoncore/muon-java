package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.extension.amqp.discovery.AmqpDiscovery;
import org.muoncore.transports.MuonResourceEvent;
import org.muoncore.MuonService;
import org.muoncore.extension.amqp.AmqpTransportExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class UserService {

    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, IOException, URISyntaxException {

        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));
        muon.registerExtension(new AmqpTransportExtension());

        muon.setServiceIdentifer("users");

        muon.start();

        muon.onGet("/mydata/happy", "Get Some Data", new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {
                System.out.println("Data has been asked for!");
                return "<h1>Got some user data!</h1>";
            }
        });
    }
}
