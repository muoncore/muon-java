package io.muoncore.transport.amqp;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.extension.amqp.*;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.MuonTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class AmqpMuonTransportFactory implements MuonTransportFactory {

    public static final String TRANSPORT_URL_PROPERTY_NAME = "amqp.transport.url";
    private static Logger LOG = LoggerFactory.getLogger(AmqpMuonTransportFactory.class.getName());
    private AutoConfiguration autoConfiguration;

    @Override
    public MuonTransport build(Properties properties) {
        MuonTransport muonTransport = null;
        try {
            String amqpUrl = properties.getProperty(TRANSPORT_URL_PROPERTY_NAME);
            if (amqpUrl == null || amqpUrl.trim().length() == 0) {
                amqpUrl = "amqp://localhost";
                LOG.warn(TRANSPORT_URL_PROPERTY_NAME + " is not set, defaulting to 'amqp://localhost' for AMQP transport connection");
            }

            String serviceName = autoConfiguration.getServiceName();
            AmqpConnection connection = new RabbitMq09ClientAmqpConnection(amqpUrl);
            QueueListenerFactory queueFactory = new RabbitMq09QueueListenerFactory(connection.getChannel());
            ServiceQueue serviceQueue = new DefaultServiceQueue(serviceName, connection);
            AmqpChannelFactory channelFactory = new DefaultAmqpChannelFactory(serviceName, queueFactory, connection);

            muonTransport = new AMQPMuonTransport(amqpUrl, serviceQueue, channelFactory);
        } catch (Exception e) {
            LOG.warn("Error creating AMQP muon transport", e);
        }
        return muonTransport;
    }

    @Override
    public void setAutoConfiguration(AutoConfiguration autoConfiguration) {
        this.autoConfiguration = autoConfiguration;
    }
}
