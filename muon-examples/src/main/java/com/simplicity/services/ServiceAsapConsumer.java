package com.simplicity.services;

import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.SingleTransportMuon;
import io.muoncore.codec.Codecs;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.extension.amqp.*;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.ServiceCache;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all;

public class ServiceAsapConsumer {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException {

        String serviceName = "awesomeService";

        AmqpConnection connection = new RabbitMq09ClientAmqpConnection("amqp://muon:microservices@localhost");
        QueueListenerFactory queueFactory = new RabbitMq09QueueListenerFactory(connection.getChannel());
        ServiceQueue serviceQueue = new DefaultServiceQueue(serviceName, connection);
        AmqpChannelFactory channelFactory = new DefaultAmqpChannelFactory(serviceName, queueFactory, connection);

        MuonTransport svc1 = new AMQPMuonTransport(
                "amqp://muon:microservices@localhost", serviceQueue, channelFactory);

        AutoConfiguration config = new AutoConfiguration();
        config.setServiceName(serviceName);
        config.setAesEncryptionKey("abcde12345678906");

        Muon muon = new SingleTransportMuon(config, createDiscovery(), svc1);

        //allow discovery settle time.
        Thread.sleep(5000);

        muon.handleRequest(all(), Map.class, response -> {
            response.ok("Hellow");
        });
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
