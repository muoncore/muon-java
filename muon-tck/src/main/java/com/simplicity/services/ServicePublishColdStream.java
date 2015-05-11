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
                new AmqpDiscovery("amqp://localhost"));

        muon.setServiceIdentifer("cl");
        new AmqpTransportExtension("amqp://localhost").extend(muon);
        muon.start();

        muon.streamSource("/counter", Integer.class, new MuonStreamGenerator<Integer>() {
            @Override
            public Publisher<Integer> generatePublisher(Map<String, String> parameters) {
                int max = Integer.parseInt(parameters.get("max"));
                return Streams.range(0, max);
            }
        });

        muon.streamSource("/countersimple", Integer.class, Streams.range(0, 100));
    }
}
