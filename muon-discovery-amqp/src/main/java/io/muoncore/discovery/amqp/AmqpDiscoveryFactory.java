package io.muoncore.discovery.amqp;

import io.muoncore.Discovery;
import io.muoncore.codec.Codecs;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.discovery.DiscoveryFactory;
import io.muoncore.extension.amqp.AmqpConnection;
import io.muoncore.extension.amqp.QueueListenerFactory;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.transport.ServiceCache;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class AmqpDiscoveryFactory implements DiscoveryFactory {
    public static final String DISCOVERY_URL_PROPERTY_NAME = "amqp.discovery.url";
    private static Logger LOG = LoggerFactory.getLogger(AmqpDiscoveryFactory.class.getName());

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
            LOG.info("Error creating AMQP discovery", e);
        }
        return discovery;
    }

    @Override
    public void setAutoConfiguration(AutoConfiguration autoConfiguration) {
    }
}
