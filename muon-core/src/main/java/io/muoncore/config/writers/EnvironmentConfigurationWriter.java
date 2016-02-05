package io.muoncore.config.writers;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.AutoConfigurationWriter;

public class EnvironmentConfigurationWriter implements AutoConfigurationWriter {

    @Override
    public void writeConfiguration(AutoConfiguration config) {
        System.getenv().forEach((key, val) -> {
            String newKey = key.replaceAll("_", ".").toLowerCase();
            config.getProperties().put(newKey, val);
        });
    }
}
