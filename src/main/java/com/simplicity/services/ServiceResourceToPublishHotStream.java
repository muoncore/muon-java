package com.simplicity.services;

import org.eclipse.jetty.util.ajax.JSON;
import org.muoncore.Muon;
import org.muoncore.MuonService;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import org.muoncore.extension.http.HttpTransportExtension;
import org.muoncore.transports.MuonResourceEvent;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

import java.net.URISyntaxException;

public class ServiceResourceToPublishHotStream {

    public static void main(String[] args) throws URISyntaxException {

        final Muon muon = new Muon();

        muon.setServiceIdentifer("resourcePublisher");
        muon.registerExtension(new AmqpTransportExtension());
        muon.registerExtension(new HttpTransportExtension(6599));
        muon.start();

        final HotStream stream = Streams.defer();

        muon.onGet("/data", "", new MuonService.MuonGet() {
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
