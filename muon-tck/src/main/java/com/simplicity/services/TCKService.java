package com.simplicity.services;

import org.eclipse.jetty.util.ajax.JSON;
import org.muoncore.*;
import org.muoncore.extension.amqp.discovery.AmqpDiscovery;
import org.muoncore.extension.amqp.AmqpTransportExtension;
import org.muoncore.extension.http.HttpTransportExtension;
import org.muoncore.extension.zeromq.ZeroMQExtension;
import org.muoncore.transports.MuonMessageEvent;
import org.muoncore.transports.MuonMessageEventBuilder;
import org.muoncore.transports.MuonResourceEvent;
import org.reactivestreams.Publisher;
import reactor.rx.Streams;
import org.muoncore.extension.streamcontrol.StreamControlExtension;

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
        muon.registerExtension(new AmqpTransportExtension("amqp://localhost:5672"));
//        muon.registerExtension(new StreamControlExtension());
        muon.start();

        final List events = Collections.synchronizedList(new ArrayList());
        final List<Map> queueEvents = Collections.synchronizedList(new ArrayList<Map>());

        Publisher pub = Streams.range(1, 10);

        muon.streamSource("myStream", pub);

        muon.onQueue("tckQueue", Map.class, new MuonService.MuonListener<Map>() {
            @Override
            public void onEvent(MuonMessageEvent<Map> event) {
                queueEvents.clear();
                queueEvents.add(event.getDecodedContent());
            }
        });
        muon.onQueue("tckQueueSend", Map.class, new MuonService.MuonListener<Map>() {
            @Override
            public void onEvent(MuonMessageEvent<Map> event) {
                Map data = event.getDecodedContent();
                muon.sendMessage(
                        MuonMessageEventBuilder.named(
                                (String) data.get("data")).withNoContent().build());
            }
        });

        muon.onGet("/tckQueueRes", Map.class, new MuonService.MuonGet<Map>() {
            @Override
            public Object onQuery(MuonResourceEvent<Map> queryEvent) {
                return queueEvents;
            }
        });

        muon.receive("echoBroadcast", new MuonService.MuonListener() {
            public void onEvent(MuonMessageEvent event) {
                muon.emit(
                        MuonMessageEventBuilder.named("echoBroadcastResponse")
                                .withContent(event.getDecodedContent().toString())
                                .build()
                );
            }
        });

        muon.receive("tckBroadcast", new MuonService.MuonListener() {
            public void onEvent(MuonMessageEvent event) {
                events.add(JSON.parse(event.getDecodedContent().toString()));
            }
        });

        muon.onGet("/event", Map.class, new MuonService.MuonGet<Map>() {
            @Override
            public Object onQuery(MuonResourceEvent<Map> queryEvent) {
                return events;
            }
        });

        muon.onGet("/event", Map.class, new MuonService.MuonGet<Map>() {
            @Override
            public Object onQuery(MuonResourceEvent<Map> queryEvent) {
                return events;
            }
        });
        muon.onDelete("/event", Map.class, new MuonService.MuonDelete<Map>() {
            @Override
            public Object onCommand(MuonResourceEvent<Map> queryEvent) {
                events.clear();
                return new Object();
            }
        });

        muon.onGet("/echo", Map.class, new MuonService.MuonGet<Map>() {
            @Override
            public Object onQuery(MuonResourceEvent<Map> queryEvent) {
                Map obj = queryEvent.getDecodedContent();

                obj.put("method", "GET");

                return obj;
            }
        });

        muon.onPost("/echo", Map.class, new MuonService.MuonPost<Map>() {
            @Override
            public Object onCommand(MuonResourceEvent<Map> queryEvent) {
                Map obj = queryEvent.getDecodedContent();

                obj.put("method", "POST");

                return obj;
            }
        });

        muon.onPut("/echo", Map.class, new MuonService.MuonPut<Map>() {
            @Override
            public Object onCommand(MuonResourceEvent<Map> queryEvent) {
                //todo, something more nuanced. Need to return a onGet url as part of the creation.

                Map obj = queryEvent.getDecodedContent();

                obj.put("method", "PUT");

                return obj;
            }
        });

        muon.onDelete("/echo", Map.class, new MuonService.MuonDelete<Map>() {
            @Override
            public Object onCommand(MuonResourceEvent<Map> queryEvent) {

                Map obj = queryEvent.getDecodedContent();

                obj.put("method", "DELETE");

                return obj;
            }
        });

        muon.onGet("/discover", Map.class, new MuonService.MuonGet<Map>() {
            @Override
            public Object onQuery(MuonResourceEvent<Map> queryEvent) {
                List<String> ids = new ArrayList<String>();

                for (ServiceDescriptor desc: muon.discoverServices()) {
                    ids.add(desc.getIdentifier());
                }

                return ids;
            }
        });
    }
}
