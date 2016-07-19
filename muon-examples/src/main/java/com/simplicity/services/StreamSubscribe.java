package com.simplicity.services;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.exception.MuonException;
import io.muoncore.protocol.event.Event;
import reactor.rx.broadcast.Broadcaster;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class StreamSubscribe {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException, ExecutionException {

        String serviceName = "awesomeServiceQuery";

        AutoConfiguration config = MuonConfigBuilder
                .withServiceIdentifier(serviceName)
                .withTags("node", "awesome")
                .build();

        Muon muon = MuonBuilder.withConfig(config).build();

        muon.getDiscovery().blockUntilReady();

        Broadcaster<Event<Map>> b = Broadcaster.create();

        b.consume(o -> {
            System.out.println("GOt DATA " + o);
        });

        b.observeError(MuonException.class, (o, e) -> {
            System.out.println ("Exception!");
            e.printStackTrace();
        });
        b.observeComplete(aVoid -> {
            System.out.println ("Completed .... ");
        });
        b.observeCancel(aVoid -> System.out.println("Cancelled by remote"));



//        EventClient ev = new DefaultEventClient(muon);
//
//        ev.replay("something", EventReplayMode.REPLAY_THEN_LIVE, Map.class, b);

        muon.subscribe(new URI("stream://stream-test/livedata"), Map.class, b);

        //muon.shutdown();
    }
}
