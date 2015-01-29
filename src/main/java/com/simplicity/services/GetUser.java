package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.MuonService;
import org.muoncore.extension.amqp.AmqpDiscovery;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import org.muoncore.transports.MuonResourceEvent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class GetUser {

    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, IOException, URISyntaxException {

        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));
        muon.registerExtension(new AmqpTransportExtension());

        muon.setServiceIdentifer("userchecker");
        muon.start();

        String data = muon.get("muon://users/mydata/happy").getResponseEvent().getPayload().toString();

        System.out.println ("We had data " + data);
    }
}
