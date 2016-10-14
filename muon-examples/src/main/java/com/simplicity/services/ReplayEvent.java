package com.simplicity.services;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.client.DefaultEventClient;
import io.muoncore.protocol.event.client.EventClient;
import io.muoncore.protocol.event.client.EventReplayMode;
import io.muoncore.protocol.reactivestream.server.PublisherLookup;
import io.muoncore.protocol.requestresponse.server.HandlerPredicates;
import reactor.rx.broadcast.Broadcaster;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ReplayEvent {

    static int eventCount = 0;

    public static void main(String[] args) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException, ExecutionException {

        AutoConfiguration config = MuonConfigBuilder
                .withServiceIdentifier("awesomeservice")
                .withTags("node", "awesome")
                .build();

        Muon muon = MuonBuilder.withConfig(config).build();

        EventClient client = new DefaultEventClient(muon);

        muon.getDiscovery().blockUntilReady();

        long then = System.currentTimeMillis();

        Broadcaster<Event<Map>> b = Broadcaster.create();
        Broadcaster<Event<Map>> output = Broadcaster.create();


        muon.publishSource("events", PublisherLookup.PublisherType.HOT, output);

        muon.handleRequest(HandlerPredicates.all(), wrapper -> wrapper.ok(Collections.singletonMap("Event Count", eventCount)));

        b.consume(mapEvent -> {
            System.out.println(mapEvent);
            eventCount++;
            try {
                output.accept(mapEvent);
            } catch (Exception ex) {}
        });

        client.replay("something", EventReplayMode.REPLAY_THEN_LIVE, Map.class, b);

        long now = System.currentTimeMillis();

        System.out.println("Took " + (now - then) + "ms");
    }
}
