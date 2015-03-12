package com.simplicity.services;

import org.muoncore.Muon;
import org.muoncore.MuonService;
import org.muoncore.extension.amqp.discovery.AmqpDiscovery;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import org.muoncore.extension.http.HttpTransportExtension;
import org.muoncore.transports.MuonResourceEvent;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class ServiceResourceToPublishHotStream {

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {

        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));

        muon.setServiceIdentifer("resourcePublisher");
        muon.registerExtension(new AmqpTransportExtension("amqp://localhost:5672"));
        muon.registerExtension(new HttpTransportExtension(6599));
        muon.start();

        final HotStream stream = Streams.defer();

        muon.onGet("/data", Map.class, new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {

                String msg = "{\"message\":\"message received\"}";
                stream.accept(msg);
                return msg;
            }
        });

        muon.streamSource("/livedata", stream);
    }
}
