package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.extension.amqp.AmqpDiscovery;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import reactor.function.Consumer;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class ServiceStreamSink {

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {

        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));

        muon.setServiceIdentifer("sink");
        muon.registerExtension(new AmqpTransportExtension());
        muon.start();

        HotStream sub = Streams.defer();

        muon.streamSink("/awesome", sub);

        sub.consume(new Consumer() {
            @Override
            public void accept(Object o) {
                System.out.println("MSG ... " + o);
            }
        });

    }
}
