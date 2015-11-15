package io.muoncore.transport.amqp;

import io.muoncore.extension.amqp.*;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.MuonTransportFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AmqpMuonTransportFactory implements MuonTransportFactory {

    public static final String DISCOVERY_URL_PROPERTY_NAME = "transport.amqp.discoveryUrl";
    private static Logger LOG = Logger.getLogger(AmqpMuonTransportFactory.class.getName());

    @Override
    public MuonTransport build(Properties properties) {
        MuonTransport muonTransport = null;
        try {
            final String discoveryUrl = properties.getProperty(DISCOVERY_URL_PROPERTY_NAME);
            final String serviceName = properties.getProperty(SERVICE_NAME_PROPERTY_NAME);
            if (discoveryUrl != null && serviceName != null) {
                AmqpConnection connection = new RabbitMq09ClientAmqpConnection(discoveryUrl);
                QueueListenerFactory queueFactory = new RabbitMq09QueueListenerFactory(connection.getChannel());
                ServiceQueue serviceQueue = new DefaultServiceQueue(serviceName, connection);
                AmqpChannelFactory channelFactory = new DefaultAmqpChannelFactory(serviceName, queueFactory, connection);

                muonTransport = new AMQPMuonTransport(discoveryUrl, serviceQueue, channelFactory);
            }
        } catch (URISyntaxException | KeyManagementException | IOException | NoSuchAlgorithmException e) {
            LOG.log(Level.WARNING, "Error creating AMQP muon transport", e);
        }
        return muonTransport;
    }
}
