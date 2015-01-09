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

public class Service2 {

    public static void main(String[] args) throws URISyntaxException {

        final Muon muon = new Muon();

        muon.setServiceIdentifer("cl");
        muon.registerExtension(new AmqpTransportExtension());
        muon.start();

        Stream stream = Streams.range(1, 200);

        muon.publishStream("/counter", stream);


        HotStream sub = Streams.defer();

        muon.subscribe("muon://cl/counter", sub);

        sub.consume(new Consumer() {
            @Override
            public void accept(Object o) {
                System.out.println("I have a message " + o);
            }
        });

    }
}
