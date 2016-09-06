package com.simplicity.services;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.message.MuonMessage;
import io.muoncore.protocol.reactivestream.server.PublisherLookup;
import io.muoncore.protocol.requestresponse.server.HandlerPredicates;
import reactor.core.processor.CancelException;
import reactor.rx.Streams;
import reactor.rx.broadcast.Broadcaster;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

public class ServicePublishColdStream {

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {

        String serviceName = "awesomeservicequery";

        AutoConfiguration config = MuonConfigBuilder
                .withServiceIdentifier(serviceName)
                .withTags("node", "awesome")
                .build();

        Muon muon = MuonBuilder.withConfig(config).build();

        muon.getDiscovery().blockUntilReady();

        muon.handleRequest(HandlerPredicates.path("/ping"), wrapper -> {
            System.out.println("Got data");
            wrapper.ok("OK THERE");
        });

        muon.publishSource("/counter", PublisherLookup.PublisherType.COLD, Streams.range(0, 100));

//        muon.handleRequest(HandlerPredicates.all(), wrapper -> {
//            wrapper.ok("HELLO WORLD");
//        });

        Broadcaster b = Broadcaster.create();

        muon.publishSource("/ticktock", PublisherLookup.PublisherType.HOT, b);

        Broadcaster<MuonMessage> tap = Broadcaster.create();
        tap.consume(o -> {
            System.out.println("TAP:From [" + o.getSourceServiceName() + "]Message is of step " + o.getStep());
        });

        muon.getTransportControl().tap(m -> true).subscribe(tap);


        Thread t = new Thread(() -> {
            while(true) {
                try {
                    try {
                        b.accept(Collections.singletonMap("time", "hello " + System.currentTimeMillis()));
                    } catch (CancelException e) {}
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();
    }
}
