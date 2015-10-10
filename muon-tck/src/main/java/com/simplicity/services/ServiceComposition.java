package com.simplicity.services;

import io.muoncore.crud.OldMuon;
import io.muoncore.crud.MuonClient;
import reactor.fn.Consumer;
import reactor.fn.Function;
import reactor.rx.Stream;
import reactor.rx.Streams;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ServiceComposition {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException {





        FutureTask<Map> val1 = new FutureTask<Map>(new Callable<Map>() {
            @Override
            public Map call() throws Exception {
                Thread.sleep(500);
                HashMap map = new HashMap();
                map.put("NEW", "DATA!");
                return map;
            }
        });

        FutureTask<Map> val2 = new FutureTask<Map>(new Callable<Map>() {
            @Override
            public Map call() throws Exception {
                Thread.sleep(1500);
                HashMap map = new HashMap();
                map.put("My Data", "Is Old");
                return map;
            }
        });

        Executor exec = Executors.newFixedThreadPool(10);
        exec.execute(val1);
        exec.execute(val2);

        Stream<List<Map>> aggregate =
                Streams.join(
                        Streams.from(val1).timeout(1, TimeUnit.SECONDS, Streams.just(new HashMap())),
                        Streams.from(val2).timeout(1, TimeUnit.SECONDS, Streams.just(new HashMap())));

        aggregate.consume(new Consumer<List<Map>>() {
            @Override
            public void accept(List<Map> maps) {
                System.out.println("Got " + maps.size());
            }
        });

        Object obj = new Object();
        synchronized (obj) {
            obj.wait();
        }



//        final Muon muon = new Muon(
//                new AmqpDiscovery("amqp://localhost:5672"));
//
//        muon.setServiceIdentifer("consumer-" + UUID.randomUUID().toString());
//        new AmqpTransportExtension("amqp://localhost:5672").extend(muon);
//        muon.start();

        //allow discovery settle time.
        //Thread.sleep(5000);

        /*Stream<List<MuonClient.MuonResult<Map>>> aggregate = loadData(muon);

        final Map userData = new HashMap(); //default
        final Map orderData = new HashMap(); //default

        aggregate.consume(new Consumer<List<MuonClient.MuonResult<Map>>>() {
            @Override
            public void accept(List<MuonClient.MuonResult<Map>> result) {
                System.out.println("Received " + result.size());
            }
        });*/
    }

    private static Stream<List<Map>> loadData(
            OldMuon muon) {

        Stream<Map> s1 = Streams.wrap(
                muon.query("muon://user?name=dawson", Map.class).toPublisher())
                .map(new Function<MuonClient.MuonResult<Map>, Map>() {
                    @Override
                    public Map apply(MuonClient.MuonResult<Map> mapMuonResult) {
                        return mapMuonResult.getResponseEvent().getDecodedContent();
                    }
                })
                .timeout(500, TimeUnit.MILLISECONDS,
                        Streams.just(defaultUser()));

        Stream<Map> s2 = Streams.wrap(
                muon.query("muon://order?name=dawson", Map.class).toPublisher())
                .map(new Function<MuonClient.MuonResult<Map>, Map>() {
                    @Override
                    public Map apply(MuonClient.MuonResult<Map> mapMuonResult) {
                        return mapMuonResult.getResponseEvent().getDecodedContent();
                    }
                })
                .timeout(500, TimeUnit.MILLISECONDS,
                        Streams.just(defaultOrder()));

        return Streams.join(s1, s2);
    }

    static Map defaultUser() {
        HashMap map = new HashMap();
        map.put("username", "defaultUser");
        return map;
    }

    static Map defaultOrder() {
        HashMap map = new HashMap();
        map.put("orderId", "defaultOrder!!");
        return map;
    }
}
