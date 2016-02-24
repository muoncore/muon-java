package io.muoncore.config.writers;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.AutoConfigurationWriter;

/**
 * Set the default configuration set.
 * If these are required to be changed, then they must be overriden by one
 * of the subsequent writers.
 */
public class DefaultConfigurationWriter implements AutoConfigurationWriter {

    @Override
    public void writeConfiguration(AutoConfiguration config) {
        //default discovery
        config.getProperties().put("muon.discovery.factories",
                "io.muoncore.discovery.amqp.AmqpDiscoveryFactory");

        //default transport
        config.getProperties().put("muon.transport.factories",
                "io.muoncore.transport.amqp.AmqpMuonTransportFactory");


        //default transport connection info
        config.getProperties().put("amqp.transport.url",
                "amqp://muon:microservices@localhost");

        //default discovery connection info
        config.getProperties().put("amqp.discovery.url",
                "amqp://muon:microservices@localhost");
    }
}
