package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import reactor.function.Consumer;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

import java.net.URISyntaxException;
import java.util.UUID;

public class ServiceStreamSinkPublish {

    public static void main(String[] args) throws URISyntaxException {

        final Muon muon = new Muon();

        muon.setServiceIdentifer("sink-" + UUID.randomUUID().toString());
        muon.registerExtension(new AmqpTransportExtension());
        muon.start();

        final HotStream publisher = Streams.defer();

        muon.publish("muon://sink/awesome", publisher);

        //generate data every so often
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        Thread.sleep(5000);
                        System.out.println("Sending data");
                        publisher.accept("I am a teapot " + System.currentTimeMillis());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
