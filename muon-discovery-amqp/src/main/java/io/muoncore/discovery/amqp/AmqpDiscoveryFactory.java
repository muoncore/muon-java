package io.muoncore.discovery.amqp;

import io.muoncore.Discovery;
import io.muoncore.codec.Codecs;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.discovery.DiscoveryFactory;
import io.muoncore.extension.amqp.AmqpConnection;
import io.muoncore.extension.amqp.QueueListenerFactory;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.extension.amqp.discovery.ServiceCache;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AmqpDiscoveryFactory implements DiscoveryFactory {
    public static final String DISCOVERY_URL_PROPERTY_NAME = "amqp.discoveryUrl";
    private static Logger LOG = Logger.getLogger(AmqpDiscoveryFactory.class.getName());
    private AutoConfiguration autoConfiguration;

    @Override
    public Discovery build(Properties properties) {
        AmqpDiscovery discovery = null;
        try {
            final String discoveryUrl = properties.getProperty(DISCOVERY_URL_PROPERTY_NAME);

            if (discoveryUrl != null) {
                AmqpConnection connection = new RabbitMq09ClientAmqpConnection(discoveryUrl);
                QueueListenerFactory queueFactory = new RabbitMq09QueueListenerFactory(connection.getChannel());
                Codecs codecs = new JsonOnlyCodecs();

                discovery = new AmqpDiscovery(queueFactory, connection, new ServiceCache(), codecs);
                discovery.start();
            }

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error creating AMQP discovery", e);
        }
        return discovery;
    }

    @Override
    public void setAutoConfiguration(AutoConfiguration autoConfiguration) {
        this.autoConfiguration = autoConfiguration;
    }
}
