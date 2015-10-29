package com.simplicity.services;

import io.muoncore.crud.OldMuon;
import io.muoncore.extension.amqp.AmqpTransportExtension;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.fn.Consumer;
import reactor.rx.broadcast.Broadcaster;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServiceStreamConsumerReconnect {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException {

        final OldMuon muon = new OldMuon(
                new AmqpDiscovery("amqp://localhost:5672"));

        muon.setServiceIdentifer("consumer-" + UUID.randomUUID().toString());
        new AmqpTransportExtension("amqp://localhost:5672").extend(muon);
        muon.start();

        //amqp discovery settle time.
        Thread.sleep(5000);

        int counter = 1;
        connect(muon, counter++);
        connect(muon, counter++);
//        connect(muon, counter++);
//        connect(muon, counter++);
//        connect(muon, counter++);
    }

    private static void connect(final OldMuon muon, final int rand) throws URISyntaxException {
        final Broadcaster<Map> sub = Broadcaster.create();
        Map<String,String> params  = new HashMap<String, String>();

        params.put("max", "5");

        muon.subscribe("muon://cl/counter", Map.class, params, sub);

        sub.consume(new Consumer<Map>() {
            @Override
            public void accept(Map o) {
                System.out.println(rand + " : I have a message " + o);
            }
        });

        sub.subscribe(
                new Subscriber<Map>() {
                    public void onSubscribe(Subscription s) {
                        System.out.println("Stream subscribed " + rand);
                    }
                    public void onNext(Map consume) {}
                    public void onError(Throwable t) {
                        System.out.println("Stream completed with ERROR " + rand);
                        t.printStackTrace();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                    connect(muon, rand);
                                } catch (URISyntaxException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    public void onComplete() {
                        System.out.println("Stream completed successfully and is disconnected");
                    }
                });
    }

    static class Consume {
        private String myname;
        private long something;

        public void setMyname(String myname) {
            this.myname = myname;
        }

        public void setSomething(long something) {
            this.something = something;
        }

        public long getSomething() {
            return something;
        }

        public String getMyname() {
            return myname;
        }

        @Override
        public String toString() {
            return "Consume{" +
                    "myname='" + myname + '\'' +
                    ", something=" + something +
                    '}';
        }
    }
}
