package com.simplicity.services;

import org.muoncore.*;
import org.muoncore.extension.amqp.discovery.AmqpDiscovery;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import reactor.function.Consumer;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServiceStreamConsumer {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException {

        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));

        muon.setServiceIdentifer("consumer-" + UUID.randomUUID().toString());
        muon.registerExtension(new AmqpTransportExtension("amqp://localhost:5672"));
        muon.start();

        //allow discovery settle time.
        Thread.sleep(5000);

        HotStream sub = Streams.defer();

        Map<String,String> params  = new HashMap<String, String>();

        params.put("max", "500");

        muon.subscribe("muon://cl/counter", params, sub);

        sub.consume(new Consumer() {
            @Override
            public void accept(Object o) {
                System.out.println("I have a message " + o);
            }
        });

    }
}
