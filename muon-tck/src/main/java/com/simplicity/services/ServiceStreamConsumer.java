package com.simplicity.services;

import io.muoncore.*;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.extension.amqp.AmqpTransportExtension;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.function.Consumer;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

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

        //allow discovery settle time.
        Thread.sleep(5000);

        HotStream<Consume> sub = Streams.defer();

        Map<String,String> params  = new HashMap<String, String>();

        params.put("max", "500");

        muon.subscribe("muon://cl/counter", Consume.class, params, sub);

        sub.consume(new Consumer<Consume>() {
            @Override
            public void accept(Consume o) {
                System.out.println("I have a message " + o);
            }
        });

        sub.subscribe(
                new Subscriber<Consume>() {
                    @Override
                    public void onSubscribe(Subscription s) {

                    }

                    @Override
                    public void onNext(Consume consume) {

                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println("Stream completed with ERROR");
                        t.printStackTrace();
                    }

                    @Override
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
