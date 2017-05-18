package io.muoncore.examples;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.reactivestream.client.StreamData;
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

        String serviceName = "clientstuff";

        AutoConfiguration config = MuonConfigBuilder
                .withServiceIdentifier(serviceName)
                .withTags("node", "awesome")
                .build();

        Muon muon = MuonBuilder.withConfig(config).build();

        muon.getDiscovery().blockUntilReady();

        createSub(muon);
        createSub(muon);
        createSub(muon);
        createSub(muon);
        createSub(muon);
        createSub(muon);
        createSub(muon);
        createSub(muon);
    }

    static void createSub(Muon muon) throws URISyntaxException {
        Broadcaster<StreamData> b = Broadcaster.create();

        b.consume(o -> {
            System.out.println("GOt DATA " + o);
        });

        b.observeError(Exception.class, (o, e) -> {
            System.out.println ("Exception!");
            e.printStackTrace();
        });
        b.observeComplete(aVoid -> {
            System.out.println ("Completed .... ");
        });
        b.observeCancel(aVoid -> System.out.println("Cancelled by remote"));
        muon.subscribe(new URI("stream://awesomeservicequery/ticktock"), b);
    }
}
