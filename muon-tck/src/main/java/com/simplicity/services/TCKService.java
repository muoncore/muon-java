package com.simplicity.services;

import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.ServiceDescriptor;
import io.muoncore.SingleTransportMuon;
import io.muoncore.codec.Codecs;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.extension.amqp.*;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.extension.amqp.discovery.ServiceCache;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory;
import io.muoncore.protocol.reactivestream.server.PublisherLookup;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.transport.MuonTransport;
import org.reactivestreams.Publisher;
import reactor.rx.Streams;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.path;

/**
 * An implementation of the Muon HTTP TCK Resources to prove compatibility of the library
 */
public class TCKService {

    public static void main(String[] args) throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException, InterruptedException {

        String serviceName = "tckservice";

        AmqpConnection connection = new RabbitMq09ClientAmqpConnection("amqp://muon:microservices@localhost");
        QueueListenerFactory queueFactory = new RabbitMq09QueueListenerFactory(connection.getChannel());
        ServiceQueue serviceQueue = new DefaultServiceQueue(serviceName, connection);
        AmqpChannelFactory channelFactory = new DefaultAmqpChannelFactory(serviceName, queueFactory, connection);

        Discovery discovery = createDiscovery();

        MuonTransport svc1 = new AMQPMuonTransport(
                "amqp://muon:microservices@localhost", serviceQueue, channelFactory);

        AutoConfiguration config = new AutoConfiguration();
        config.setServiceName(serviceName);
        config.setAesEncryptionKey("abcde12345678906");

        Muon muon = new SingleTransportMuon(config, discovery, svc1);

        //allow discovery settle time.
        Thread.sleep(5000);

        outboundResourcesSetup(muon);

        inboundResourcesSetup(muon);

        streamPublisher(muon);

    }

    private static void outboundResourcesSetup(final Muon muon) {

        final Map storedata = new HashMap();

        muon.handleRequest(path("/invokeresponse-store"), Map.class, queryEvent -> queryEvent.ok(storedata) );

        muon.handleRequest(path("/invokeresponse"), Map.class, queryEvent -> {

                String url = (String) queryEvent.getRequest().getPayload().get("resource");

                Response<Map> rsult = null;
                try {
                    rsult = muon.request(url, Map.class).get();
                    storedata.clear();
                    storedata.putAll(rsult.getPayload());
                    queryEvent.ok(rsult.getPayload());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        );
    }

    private static void streamPublisher(Muon muon) {
        Publisher<Long> pub = Streams.range(1, 10);
        muon.publishSource("/myStream", PublisherLookup.PublisherType.COLD, pub);
    }

    private static void inboundResourcesSetup(final Muon muon) {
        muon.handleRequest(path("/echo"), Map.class, queryEvent -> {
                Map obj = queryEvent.getRequest().getPayload();

                obj.put("method", "GET");

                queryEvent.ok(obj);
            });

        muon.handleRequest(path("/discover"), Map.class, request ->
                request.ok(
                        muon.getDiscovery().getKnownServices().stream().map(ServiceDescriptor::getIdentifier).collect(Collectors.toList())));
    }

    private static Discovery createDiscovery() throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException {

        AmqpConnection connection = new RabbitMq09ClientAmqpConnection("amqp://muon:microservices@localhost");
        QueueListenerFactory queueFactory = new RabbitMq09QueueListenerFactory(connection.getChannel());
        Codecs codecs = new JsonOnlyCodecs();

        AmqpDiscovery discovery = new AmqpDiscovery(queueFactory, connection, new ServiceCache(), codecs);
        discovery.start();
        return discovery;
    }

}
