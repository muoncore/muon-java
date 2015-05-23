package com.simplicity.services;

import io.muoncore.*;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.extension.amqp.AmqpTransportExtension;
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

public class ServiceStreamConsumer {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException {

        final Muon muon = new Muon(
                new AmqpDiscovery("amqp://localhost:5672"));

        muon.setServiceIdentifer("consumer-" + UUID.randomUUID().toString());
        new AmqpTransportExtension("amqp://localhost:5672").extend(muon);
        muon.start();

        //amqp discovery settle time.
        Thread.sleep(5000);

        Broadcaster<Map> sub = Broadcaster.create();

        Map<String,String> params  = new HashMap<String, String>();

        params.put("max", "500");

        muon.subscribe("muon://cl/counter", Map.class, params, sub);

        sub.consume(new Consumer<Map>() {
            @Override
            public void accept(Map o) {
                System.out.println("I have a message " + o);
            }
        });

        sub.subscribe(
                new Subscriber<Map>() {
                    public void onSubscribe(Subscription s) {}
                    public void onNext(Map consume) {}
                    public void onError(Throwable t) {
                        System.out.println("Stream completed with ERROR");
                        t.printStackTrace();
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
