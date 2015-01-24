package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import reactor.function.Consumer;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServiceAsapConsumer {

    public static void main(String[] args) throws URISyntaxException, InterruptedException {

        final Muon muon = new Muon();

        muon.setServiceIdentifer("consumer-" + UUID.randomUUID().toString());
        muon.registerExtension(new AmqpTransportExtension());
        muon.start();

        //allow discovery settle time.
        Thread.sleep(5000);

        HotStream sub = Streams.defer();

        Map<String,String> params  = new HashMap<String, String>();

        params.put("max", "500");

        muon.subscribe("muon://asapcore/pipe", params, sub);

        sub.consume(new Consumer() {
            @Override
            public void accept(Object o) {
                System.out.println("I have a message " + o);
            }
        });

    }
}
