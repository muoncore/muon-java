package com.simplicity.services;

import io.muoncore.Muon;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.extension.amqp.AmqpTransportExtension;
import reactor.function.Consumer;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ServiceAsapConsumer {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException {

        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));

        muon.setServiceIdentifer("consumer-" + UUID.randomUUID().toString());
        new AmqpTransportExtension("amqp://localhost:5672").extend(muon);
        muon.start();

        //allow discovery settle time.
        Thread.sleep(5000);

        HotStream<Map> sub = Streams.defer();

        Map<String,String> params  = new HashMap<String, String>();

//        params.put("max", "500");

        muon.subscribe("muon://asapcore/events", Map.class, params, sub);

        final List items = new ArrayList();

        sub.consume(new Consumer<Map>() {
            @Override
            public void accept(Map o) {
                items.add(o);
                System.out.println("I have a message " + o);
            }
        });

        sub.drain();

        System.out.println("Items were ... " + items.size());
    }
}
