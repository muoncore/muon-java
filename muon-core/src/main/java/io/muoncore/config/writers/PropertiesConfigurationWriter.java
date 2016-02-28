package io.muoncore.config.writers;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.AutoConfigurationWriter;

import java.util.Properties;

public class PropertiesConfigurationWriter implements AutoConfigurationWriter {
    private Properties properties;

    public PropertiesConfigurationWriter(Properties props) {
        this.properties = props;
    }

    @Override
    public void writeConfiguration(AutoConfiguration config) {
        config.getProperties().putAll(properties);
    }
}
