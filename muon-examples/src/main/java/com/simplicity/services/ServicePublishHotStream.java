package com.simplicity.services;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.reactivestream.server.PublisherLookup;
import reactor.rx.broadcast.Broadcaster;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class ServicePublishHotStream {

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {

        String serviceName = "awesomeservicequery";

        AutoConfiguration config = MuonConfigBuilder
                .withServiceIdentifier(serviceName)
                .withTags("node", "awesome")
                .build();

        Muon muon = MuonBuilder.withConfig(config).build();

        muon.getDiscovery().blockUntilReady();

        muon.publishGeneratedSource("/hello", PublisherLookup.PublisherType.HOT, subscriptionRequest -> {
            Broadcaster b = Broadcaster.create();

            new Thread(() -> {
                while (true) {
                    System.out.println("SENDING!");
                    try {
                        b.accept("HELLO");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            return b;
        });
    }

//    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {
//
//        final OldMuon muon = new OldMuon(
//                new AmqpDiscovery("amqp://muon:microservices@localhost:5672"));
//
//        muon.setServiceIdentifer("cl");
//        new AmqpTransportExtension("amqp://muon:microservices@localhost:5672").extend(muon);
//        muon.start();
//
//        final Broadcaster stream = Broadcaster.create();
//
//        muon.streamSource("/counter", Map.class, stream);
//
//        //generate data every so often
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(true) {
//                try {
//
//                        Thread.sleep(500);
//                        System.out.println("Sending data");
//
//                        stream.accept(new Awesome("I am a teapot", System.currentTimeMillis()));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                }
//            }
//        }).start();
//    }
//
//    static class Awesome {
//        private String myname;
//        private long something;
//
//        public Awesome(String myname, long something) {
//            this.myname = myname;
//            this.something = something;
//        }
//
//        public long getSomething() {
//            return something;
//        }
//
//        public String getMyname() {
//            return myname;
//        }
//    }
}
