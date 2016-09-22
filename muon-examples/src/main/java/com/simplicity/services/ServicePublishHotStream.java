package com.simplicity.services;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.reactivestream.server.PublisherLookup;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicInteger;

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all;

public class ServicePublishHotStream {

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {

        String serviceName = "product";

        AutoConfiguration config = MuonConfigBuilder
                .withServiceIdentifier(serviceName)
                .withTags("node", "awesome")
                .build();

        Muon muon = MuonBuilder.withConfig(config).build();

        muon.getDiscovery().blockUntilReady();

        muon.handleRequest(all(), wrapper -> {
            wrapper.ok("THANKS");
        });

        muon.publishGeneratedSource("/hello", PublisherLookup.PublisherType.HOT, subscriptionRequest -> {

            Publisher<String> pub = s -> {
                AtomicInteger cancel = new AtomicInteger(0);
                Subscription sub = new Subscription() {
                    @Override
                    public void request(long n) {
                        System.out.println("Subscriber requests data " + n);
                    }

                    @Override
                    public void cancel() {
                        System.out.println("Subscriber Cancelled");
                        cancel.addAndGet(1);
                    }
                };
                new Thread(() -> {
                    while (cancel.longValue() == 0) {
                        System.out.println("SENDING!");
                        try {
                            s.onNext("HELLO WORLD " + System.currentTimeMillis());
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
                s.onSubscribe(sub);
            };

            return pub;
        });
    }
}
