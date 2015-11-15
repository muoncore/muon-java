package io.muoncore.discovery;

import io.muoncore.Discovery;
import io.muoncore.config.AutoConfiguration;

import java.util.Properties;

public interface DiscoveryFactory {
    Discovery build(Properties properties);

    void setAutoConfiguration(AutoConfiguration autoConfiguration);
}
