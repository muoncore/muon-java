package com.simplicity.services;

import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.codec.Codecs;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.extension.amqp.AmqpConnection;
import io.muoncore.extension.amqp.QueueListenerFactory;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.transport.ServiceCache;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ServiceQuery {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException, ExecutionException {

        String serviceName = "awesomeServiceQuery";

        AutoConfiguration config = MuonConfigBuilder
                .withServiceIdentifier(serviceName)
                .withTags("node", "awesome")
                .build();

        Muon muon = MuonBuilder.withConfig(config).build();

        //allow discovery settle time.
        Thread.sleep(5000);

        Map data = new HashMap<>();

        Response ret = muon.request("request://awesomeService", data, String.class).get();

        System.out.println("Server responds " + ret.getPayload());
        muon.shutdown();
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
