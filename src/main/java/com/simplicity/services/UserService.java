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
import java.util.Map;

public class UserService {

    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, IOException, URISyntaxException {

        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));
        muon.registerExtension(new AmqpTransportExtension("amqp://localhost:5672"));

        muon.setServiceIdentifer("users");

        muon.start();

        muon.onGet("/mydata/happy", Map.class, new MuonService.MuonGet<Map>() {
            @Override
            public Object onQuery(MuonResourceEvent<Map> queryEvent) {
                System.out.println("Data has been asked for!");
                return "<h1>Got some user data!</h1>";
            }
        });
    }
}
