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
import java.util.Map;

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.path;

public class ServiceResourceToPublishHotStream {

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {

        AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("stream-test").build();

        Muon muon = MuonBuilder.withConfig(config).build();

        muon.getDiscovery().blockUntilReady();

        final Broadcaster<Map> stream = Broadcaster.create();

        muon.handleRequest(path("/in"), wrapper -> {
            Map data = wrapper.getRequest().getPayload(Map.class);
            stream.accept(data);
            wrapper.ok("thanks!");
        });

        muon.handleRequest(path("/hello"), wrapper -> {
            wrapper.ok("thanks!");
        });

        muon.publishSource("/livedata", PublisherLookup.PublisherType.HOT, stream);
    }
}
