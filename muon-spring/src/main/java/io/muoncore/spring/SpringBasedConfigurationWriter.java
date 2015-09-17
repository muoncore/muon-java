package io.muoncore.spring;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.AutoConfigurationWriter;
import org.springframework.util.StringUtils;

public class SpringBasedConfigurationWriter implements AutoConfigurationWriter {
    private final String amqpUrl;

    public SpringBasedConfigurationWriter(String amqpUrl) {
        this.amqpUrl = amqpUrl;
    }

    @Override
    public void writeConfiguration(AutoConfiguration config) {
        if (!StringUtils.isEmpty(amqpUrl)) {
            config.setDiscoveryUrl(amqpUrl);
        }
    }
}
