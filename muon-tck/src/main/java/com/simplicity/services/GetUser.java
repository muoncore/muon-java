package com.simplicity.services;

import io.muoncore.Muon;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.extension.amqp.AmqpTransportExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class GetUser {

    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, IOException, URISyntaxException {

        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));
        muon.registerExtension(new AmqpTransportExtension("amqp://localhost:5672"));

        muon.setServiceIdentifer("userchecker");
        muon.start();

        Map data = muon.get(
                "muon://users/mydata/happy", Map.class).getResponseEvent().getDecodedContent();

        System.out.println ("We had data " + data);
    }
}
