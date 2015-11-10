package io.muoncore.spring;

import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.SingleTransportMuon;
import io.muoncore.codec.Codecs;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.extension.amqp.*;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.extension.amqp.discovery.ServiceCache;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory;
import io.muoncore.transport.MuonTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class MuonConfiguration {

    @Autowired
    private AutoConfiguration muonAutoConfiguration;

    @Bean
    public MuonTransport muonTransport() throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException {
        AmqpConnection connection = new RabbitMq09ClientAmqpConnection(muonAutoConfiguration.getDiscoveryUrl());
        QueueListenerFactory queueFactory = new RabbitMq09QueueListenerFactory(connection.getChannel());
        ServiceQueue serviceQueue = new DefaultServiceQueue(muonAutoConfiguration.getServiceName(), connection);
        AmqpChannelFactory channelFactory = new DefaultAmqpChannelFactory(muonAutoConfiguration.getServiceName(), queueFactory, connection);

        return new AMQPMuonTransport(muonAutoConfiguration.getDiscoveryUrl(), serviceQueue, channelFactory);
    }

    @Bean
    public Muon muon() throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException {
        return new SingleTransportMuon(muonAutoConfiguration, muonDiscovery(), muonTransport());
    }

    @Bean
    public Discovery muonDiscovery() throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException {
        AmqpConnection connection = new RabbitMq09ClientAmqpConnection(muonAutoConfiguration.getDiscoveryUrl());
        QueueListenerFactory queueFactory = new RabbitMq09QueueListenerFactory(connection.getChannel());
        Codecs codecs = new JsonOnlyCodecs();

        AmqpDiscovery discovery = new AmqpDiscovery(queueFactory, connection, new ServiceCache(), codecs);
        discovery.start();

        return discovery;
    }
}
