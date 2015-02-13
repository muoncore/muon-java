package com.simplicity.services;

import org.eclipse.jetty.util.ajax.JSON;
import org.muoncore.*;
import org.muoncore.extension.amqp.discovery.AmqpDiscovery;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import org.muoncore.extension.http.HttpTransportExtension;
import org.muoncore.extension.zeromq.ZeroMqTransportExtension;
import org.muoncore.transports.MuonMessageEvent;
import org.muoncore.transports.MuonMessageEventBuilder;
import org.muoncore.transports.MuonResourceEvent;
import org.reactivestreams.Publisher;
import reactor.rx.Streams;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the Muon HTTP TCK Resources to prove compatibility of the library
 */
public class TCKService {

    public static void main(String[] args) throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException {

        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));

        muon.setServiceIdentifer("tck");
        muon.addTags("my-tag", "tck-service");

        muon.registerExtension(new HttpTransportExtension(7171));
        muon.registerExtension(new AmqpTransportExtension());
        muon.registerExtension(new ZeroMqTransportExtension());
        muon.start();

        final List events = Collections.synchronizedList(new ArrayList());
        final List queueEvents = Collections.synchronizedList(new ArrayList());

        Publisher pub = Streams.range(1, 10);

        muon.streamSource("myStream", pub);

        muon.onQueue("tckQueue", new MuonService.MuonListener() {
            @Override
            public void onEvent(MuonMessageEvent event) {
                queueEvents.clear();
                queueEvents.add(JSON.parse(event.getPayload().toString()));
            }
        });
        muon.onQueue("tckQueueSend", new MuonService.MuonListener() {
            @Override
            public void onEvent(MuonMessageEvent event) {
                Map data = (Map) JSON.parse(event.getPayload().toString());
                muon.sendMessage(
                        MuonMessageEventBuilder.named(
                                (String) data.get("data")).withNoContent().build());
            }
        });

        muon.onGet("/tckQueueRes", "hello", new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {
                return JSON.toString(queueEvents);
            }
        });

        muon.receive("echoBroadcast", new MuonService.MuonListener() {
            public void onEvent(MuonMessageEvent event) {
                muon.emit(
                        MuonMessageEventBuilder.named("echoBroadcastResponse")
                                .withContent(event.getPayload().toString())
                                .build()
                );
            }
        });

        muon.receive("tckBroadcast", new MuonService.MuonListener() {
            public void onEvent(MuonMessageEvent event) {
                events.add(JSON.parse(event.getPayload().toString()));
            }
        });

        muon.onGet("/event", "Get The Events", new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {
                return JSON.toString(events);
            }
        });

        muon.onGet("/event", "Get The Events", new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {
                return JSON.toString(events);
            }
        });
        muon.onDelete("/event", "Remove The Events", new MuonService.MuonDelete() {
            @Override
            public Object onCommand(MuonResourceEvent queryEvent) {
                events.clear();
                return "{}";
            }
        });

        muon.onGet("/echo", "Get Some Data", new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {
                Map obj = (Map) JSON.parse((String) queryEvent.getPayload());

                obj.put("method", "GET");

                return JSON.toString(obj);
            }
        });

        muon.onPost("/echo", "Allow posting of some data", new MuonService.MuonPost() {
            @Override
            public Object onCommand(MuonResourceEvent queryEvent) {
                Map obj = (Map) JSON.parse((String) queryEvent.getPayload());

                obj.put("method", "POST");

                return JSON.toString(obj);
            }
        });

        muon.onPut("/echo", "Allow posting of some data", new MuonService.MuonPut() {
            @Override
            public Object onCommand(MuonResourceEvent queryEvent) {
                //todo, something far more nuanced. Need to return a onGet url as part of the creation.

                Map obj = (Map) JSON.parse((String) queryEvent.getPayload());

                obj.put("method", "PUT");

                return JSON.toString(obj);
            }
        });

        muon.onDelete("/echo", "Allow deleting of some data", new MuonService.MuonDelete() {
            @Override
            public Object onCommand(MuonResourceEvent queryEvent) {

                Map obj = (Map) JSON.parse((String) queryEvent.getPayload());

                obj.put("method", "DELETE");

                return JSON.toString(obj);
            }
        });

        muon.onGet("/discover", "Show the currently discovered service identifiers.", new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {
                List<String> ids = new ArrayList<String>();

                for (ServiceDescriptor desc: muon.discoverServices()) {
                    ids.add(desc.getIdentifier());
                }

                return JSON.toString(ids);
            }
        });
    }
}
