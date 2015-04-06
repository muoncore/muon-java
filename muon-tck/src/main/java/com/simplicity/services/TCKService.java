package com.simplicity.services;

import io.muoncore.Muon;
import io.muoncore.MuonClient;
import io.muoncore.MuonService;
import io.muoncore.ServiceDescriptor;
import io.muoncore.codec.KryoExtension;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.extension.amqp.AmqpTransportExtension;
import io.muoncore.extension.http.HttpTransportExtension;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.MuonMessageEventBuilder;
import io.muoncore.transport.resource.MuonResourceEvent;
import org.reactivestreams.Publisher;
import reactor.rx.Streams;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * An implementation of the Muon HTTP TCK Resources to prove compatibility of the library
 */
public class TCKService {

    public static void main(String[] args) throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException {


        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));

        muon.setServiceIdentifer("tck");
        muon.addTags("my-tag", "tck-service");

        new HttpTransportExtension(7171).extend(muon);
        new AmqpTransportExtension("amqp://localhost:5672").extend(muon);
//        new StreamControlExtension().extend(muon);
        new KryoExtension().extend(muon);

        muon.start();

        queueSetup(muon);

        broadcastSetup(muon);

        outboundResourcesSetup(muon);

        inboundResourcesSetup(muon);

        streamPublisher(muon);

    }

    private static void outboundResourcesSetup(final Muon muon) {

        final Map storedata = new HashMap();

        muon.onGet("/invokeresponse-store", Map.class, new MuonService.MuonGet<Map>() {
            @Override
            public Object onQuery(MuonResourceEvent<Map> queryEvent) {
                return storedata;
            }
        });

        muon.onGet("/invokeresponse", Map.class, new MuonService.MuonGet<Map>() {
                    @Override
                    public Object onQuery(MuonResourceEvent<Map> queryEvent) {

                        String url = (String) queryEvent.getDecodedContent().get("resource");

                        MuonClient.MuonResult<Map> rsult = muon.get(url, Map.class);

                        Map data = rsult.getResponseEvent().getDecodedContent();

                        storedata.clear();
                        storedata.putAll(data);

                        return data;
                    }
                }
        );
    }

    private static void streamPublisher(Muon muon) {
        Publisher<Integer> pub = Streams.range(1, 10);

        muon.streamSource("myStream", Integer.class, pub);
    }

    private static void inboundResourcesSetup(final Muon muon) {
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

    private static List<Map> broadcastSetup(final Muon muon) {
        final List<Map> events = Collections.synchronizedList(new ArrayList<Map>());

        muon.receive("echoBroadcast", Map.class, new MuonService.MuonListener<Map>() {
            public void onEvent(MuonMessageEvent<Map> event) {
                muon.emit(
                        MuonMessageEventBuilder.named("echoBroadcastResponse")
                                .withContent(event.getDecodedContent())
                                .build()
                );
            }
        });

        muon.receive("tckBroadcast", Map.class, new MuonService.MuonListener<Map>() {
            public void onEvent(MuonMessageEvent<Map> event) {
                events.add(event.getDecodedContent());
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
                return new HashMap();
            }
        });

        return events;
    }

    private static void queueSetup(final Muon muon) {
        //Setup required to pass the Queue TCK. Not convinced this concept is a good idea at all.
        final List<Map> queueEvents = Collections.synchronizedList(new ArrayList<Map>());

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

    }
}
