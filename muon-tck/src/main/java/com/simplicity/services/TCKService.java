package com.simplicity.services;

import io.muoncore.*;
import io.muoncore.codec.KryoExtension;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.extension.amqp.AmqpTransportExtension;
import io.muoncore.extension.http.HttpTransportExtension;
import io.muoncore.future.MuonFuture;
import io.muoncore.future.MuonFutures;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.MuonMessageEventBuilder;
import io.muoncore.transport.resource.MuonResourceEvent;
import org.reactivestreams.Publisher;
import reactor.fn.Function;
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

        muon.onQuery("/invokeresponse-store", Map.class, new MuonService.MuonQuery<Map>() {
            @Override
            public MuonFuture onQuery(MuonResourceEvent<Map> queryEvent) {
                return MuonFutures.immediately(storedata);
            }
        });

        muon.onQuery("/invokeresponse", Map.class, new MuonService.MuonQuery<Map>() {
                @Override
                public MuonFuture onQuery(MuonResourceEvent<Map> queryEvent) {

                    String url = (String) queryEvent.getDecodedContent().get("resource");

                    MuonFuture<MuonClient.MuonResult<Map>> rsult = muon.get(url, Map.class);

                    return MuonFutures.fromPublisher(
                            Streams.wrap(rsult.toPublisher()).map(new Function<MuonClient.MuonResult<Map>, Map>() {
                                @Override
                                public Map apply(MuonClient.MuonResult<Map> mapMuonResult) {
                                    Map data = mapMuonResult.getResponseEvent().getDecodedContent();
                                    storedata.clear();
                                    storedata.putAll(data);

                                    return data;
                                }
                            }));
                }
            }
        );
    }

    private static void streamPublisher(Muon muon) {
        Publisher<Long> pub = Streams.range(1, 10);
        muon.streamSource("myStream", Long.class, pub);
    }

    private static void inboundResourcesSetup(final Muon muon) {
        muon.onQuery("/echo", Map.class, new MuonService.MuonQuery<Map>() {
            @Override
            public MuonFuture onQuery(MuonResourceEvent<Map> queryEvent) {
                Map obj = queryEvent.getDecodedContent();

                obj.put("method", "GET");

                return MuonFutures.immediately(obj);
            }
        });

        muon.onCommand("/echo", Map.class, new MuonService.MuonCommand<Map>() {
            @Override
            public MuonFuture<Map> onCommand(MuonResourceEvent<Map> queryEvent) {
                String method = queryEvent.getHeaders().get("METHOD");

                Map obj = queryEvent.getDecodedContent();

                obj.put("method", method);

                return MuonFutures.immediately(obj);
            }
        });

        muon.onQuery("/discover", Map.class, new MuonService.MuonQuery<Map>() {
            @Override
            public MuonFuture<List<String>> onQuery(MuonResourceEvent<Map> queryEvent) {
                List<String> ids = new ArrayList<String>();

                for (ServiceDescriptor desc: muon.discoverServices()) {
                    ids.add(desc.getIdentifier());
                }

                return MuonFutures.immediately(ids);
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


        muon.onQuery("/event", Map.class, new MuonService.MuonQuery<Map>() {
            @Override
            public MuonFuture onQuery(MuonResourceEvent<Map> queryEvent) {
                return MuonFutures.immediately(events);
            }
        });

        muon.onCommand("/event", Map.class, new MuonService.MuonCommand<Map>() {
            @Override
            public MuonFuture onCommand(MuonResourceEvent<Map> queryEvent) {
                events.clear();
                return MuonFutures.immediately(new HashMap());
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

        muon.onQuery("/tckQueueRes", Map.class, new MuonService.MuonQuery<Map>() {
            @Override
            public MuonFuture onQuery(MuonResourceEvent<Map> queryEvent) {
                return MuonFutures.immediately(queueEvents);
            }
        });

    }
}
