package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.extension.amqp.discovery.AmqpDiscovery;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class ServiceStreamSinkPublish {

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {

        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));

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
