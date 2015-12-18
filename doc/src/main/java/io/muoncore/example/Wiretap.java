package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.protocol.reactivestream.server.PublisherLookup;
import reactor.rx.broadcast.Broadcaster;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class Wiretap {

    public void exec(Muon muon) throws ExecutionException, InterruptedException, URISyntaxException, UnsupportedEncodingException {

        Broadcaster<String> publisher = Broadcaster.create();

        muon.publishSource("stockTicker", PublisherLookup.PublisherType.HOT, publisher);

        //some time later
        publisher.accept("APPL: 210");
        publisher.accept("NOKIA: 20");


        Broadcaster<String> client = Broadcaster.create();
        client.consume( val -> {
            System.out.println("The value is " + val);
        });

        muon.subscribe(new URI("stream://myservice/stockTicker"), String.class, client);
    }
}
