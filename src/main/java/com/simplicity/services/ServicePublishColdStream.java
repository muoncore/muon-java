package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.MuonStreamGenerator;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import org.reactivestreams.Publisher;
import reactor.rx.Stream;
import reactor.rx.Streams;

import java.net.URISyntaxException;
import java.util.Map;

public class ServicePublishColdStream {

    public static void main(String[] args) throws URISyntaxException {

        final Muon muon = new Muon();

        muon.setServiceIdentifer("cl");
        muon.registerExtension(new AmqpTransportExtension());
        muon.start();

        muon.streamSource("/counter", new MuonStreamGenerator() {
            @Override
            public Publisher generatePublisher(Map<String, String> parameters) {
                Stream stream = Streams.range(100, 200);
                return stream;
            }
        });
    }
}
