package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import reactor.rx.Stream;
import reactor.rx.Streams;

import java.net.URISyntaxException;

public class ServicePublishColdStream {

    public static void main(String[] args) throws URISyntaxException {

        final Muon muon = new Muon();

        muon.setServiceIdentifer("cl");
        muon.registerExtension(new AmqpTransportExtension());
        muon.start();

        Stream stream = Streams.range(100, 200);

        muon.streamSource("/counter", stream);

    }
}
