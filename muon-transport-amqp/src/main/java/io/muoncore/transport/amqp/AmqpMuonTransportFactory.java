package io.muoncore.transport.amqp;

import io.muoncore.Discovery;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.extension.amqp.*;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.MuonTransportFactory;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AmqpMuonTransportFactory implements MuonTransportFactory {

    public static final String DISCOVERY_URL_PROPERTY_NAME = "amqp.discoveryUrl";
    private static Logger LOG = Logger.getLogger(AmqpMuonTransportFactory.class.getName());
    private AutoConfiguration autoConfiguration;
    private Discovery discovery;

    @Override
    public MuonTransport build(Properties properties) {
        MuonTransport muonTransport = null;
        try {
            String discoveryUrl = properties.getProperty(DISCOVERY_URL_PROPERTY_NAME);
            if (discoveryUrl == null || discoveryUrl.trim().length() == 0) {
                discoveryUrl = "amqp://localhost";
                LOG.log(Level.WARNING, "amqp.discoveryUrl is not set, defaulting to 'amqp://localhost' for AMQP transport connection");
            }
            final String serviceName = autoConfiguration.getServiceName();
            if (discoveryUrl != null && serviceName != null) {
                AmqpConnection connection = new RabbitMq09ClientAmqpConnection(discoveryUrl);
                QueueListenerFactory queueFactory = new RabbitMq09QueueListenerFactory(connection.getChannel());
                ServiceQueue serviceQueue = new DefaultServiceQueue(serviceName, connection);
                AmqpChannelFactory channelFactory = new DefaultAmqpChannelFactory(serviceName, queueFactory, connection);

                muonTransport = new AMQPMuonTransport(discoveryUrl, serviceQueue, channelFactory);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error creating AMQP muon transport, properties muon.serviceName must be set.", e);
        }
        return muonTransport;
    }

    @Override
    public void setAutoConfiguration(AutoConfiguration autoConfiguration) {
        this.autoConfiguration = autoConfiguration;
    }
}
