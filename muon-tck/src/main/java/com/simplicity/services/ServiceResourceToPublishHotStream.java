package com.simplicity.services;

import io.muoncore.Muon;
import io.muoncore.MuonService;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.extension.amqp.AmqpTransportExtension;
import io.muoncore.transport.resource.MuonResourceEvent;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class ServiceResourceToPublishHotStream {

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {

        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));

        muon.setServiceIdentifer("resourcePublisher");
        new AmqpTransportExtension("amqp://localhost:5672").extend(muon);
        muon.start();

        final HotStream<Map> stream = Streams.defer();

        muon.onGet("/data", Map.class, new MuonService.MuonGet<Map>() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {

                Map<String, String> data = new HashMap<String, String>();

                stream.accept(data);

                return data;
            }
        });

        muon.streamSource("/livedata", Map.class, stream);
    }
}
