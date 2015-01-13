package com.simplicity.services;

import org.muoncore.*;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.function.Consumer;
import reactor.rx.Stream;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

import java.net.URISyntaxException;
import java.util.UUID;

public class ServiceStreamConsumer {

    public static void main(String[] args) throws URISyntaxException, InterruptedException {

        final Muon muon = new Muon();

        muon.setServiceIdentifer("consumer-" + UUID.randomUUID().toString());
        muon.registerExtension(new AmqpTransportExtension());
        muon.start();

        //allow discovery settle time.
        Thread.sleep(5000);

        HotStream sub = Streams.defer();

        muon.subscribe("muon://resourcePublisher/livedata", sub);

        sub.consume(new Consumer() {
            @Override
            public void accept(Object o) {
                System.out.println("I have a message " + o);
            }
        });

    }
}
