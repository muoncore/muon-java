package com.simplicity.services;

import io.muoncore.Muon;
import io.muoncore.MuonStreamGenerator;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.extension.amqp.AmqpTransportExtension;
import org.reactivestreams.Publisher;
import reactor.rx.Streams;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class ServicePublishColdStream {

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {

        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));

        muon.setServiceIdentifer("cl");
        muon.registerExtension(new AmqpTransportExtension("amqp://localhost:5672"));
        muon.start();

        muon.streamSource("/counter", new MuonStreamGenerator() {
            @Override
            public Publisher generatePublisher(Map<String, String> parameters) {
                int max = Integer.parseInt(parameters.get("max"));
                return Streams.range(0, max);
            }
        });
    }
}
