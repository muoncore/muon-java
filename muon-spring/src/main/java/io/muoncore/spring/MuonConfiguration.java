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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;

@Configuration
public class MuonConfiguration {

    @Autowired
    private AutoConfiguration muonAutoConfiguration;

    @Autowired
    Environment env;

    @Autowired
    private MuonTransport[] muonTransports;

//    @Autowired
//    private MuonTransport muonTransport;

    @Bean
    public Muon muon() throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, IOException {
        return new SingleTransportMuon(muonAutoConfiguration, muonDiscovery(), getValidMuonTransport(muonTransports));

    }

    //TODO Remove this method when muon multiple transports is implemented
    private MuonTransport getValidMuonTransport(MuonTransport[] muonTransports) {
        for (MuonTransport muonTransport : muonTransports) {
            if (muonTransport != null) {
                return muonTransport;
            }
        }
        throw new IllegalStateException("No muon transports found");
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
