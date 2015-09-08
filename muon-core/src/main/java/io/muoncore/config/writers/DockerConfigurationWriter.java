package io.muoncore.config.writers;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.AutoConfigurationWriter;

public class DockerConfigurationWriter  implements AutoConfigurationWriter {

    @Override
    public void writeConfiguration(AutoConfiguration config) {

        String rabbitHost = System.getenv("RABBITMQ_PORT_5672_TCP_ADDR");

        if (rabbitHost != null && rabbitHost.length() > 0) {
            config.setDiscoveryUrl("amqp://muon:microservices@" + rabbitHost);
        }
    }
}
