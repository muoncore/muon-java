package io.muoncore.config.writers;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.AutoConfigurationWriter;

/**
 * Check any docker link style configuration properties and rework them to know config formats.
 *
 * Primarily, AMQP transport
 *
 * docker.rabbitmq.port.5672.tcp.addr is aliased to amqp.transport.url and amqp.discovery.url
 *
 */
public class DockerLinkConfigurationWriter implements AutoConfigurationWriter {

    @Override
    public void writeConfiguration(AutoConfiguration config) {

        String rabbitMqUrl = config.getStringConfig("rabbitmq.port.5672.tcp.addr");

        if (config.getStringConfig("rabbitmq.port.5672.tcp.addr") != null) {
            config.getProperties().put("amqp.transport.url", rabbitMqUrl);
            config.getProperties().put("amqp.discovery.url", rabbitMqUrl);
        }
    }
}
