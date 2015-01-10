package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

import java.net.URISyntaxException;

public class ServicePublishHotStream {

    public static void main(String[] args) throws URISyntaxException {

        final Muon muon = new Muon();

        muon.setServiceIdentifer("cl");
        muon.registerExtension(new AmqpTransportExtension());
        muon.start();

        final HotStream stream = Streams.defer();

        muon.streamSource("/counter", stream);

        //generate data every so often
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        Thread.sleep(5000);
                        System.out.println("Sending data");
                        stream.accept("I am a teapot " + System.currentTimeMillis());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
