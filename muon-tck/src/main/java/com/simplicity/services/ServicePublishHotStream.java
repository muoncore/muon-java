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

public class ServicePublishHotStream {

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {

        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));

        muon.setServiceIdentifer("cl");
        muon.registerExtension(new AmqpTransportExtension("amqp://localhost:5672"));
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

                        stream.accept(new Awesome("I am a teapot", System.currentTimeMillis()));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    static class Awesome {
        private String myname;
        private long something;

        public Awesome(String myname, long something) {
            this.myname = myname;
            this.something = something;
        }

        public long getSomething() {
            return something;
        }

        public String getMyname() {
            return myname;
        }
    }
}
