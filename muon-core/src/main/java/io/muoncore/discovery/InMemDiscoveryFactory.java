package io.muoncore.discovery;

import io.muoncore.Discovery;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.memory.discovery.InMemDiscovery;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InMemDiscoveryFactory implements DiscoveryFactory {

    private static final String IN_MEM_DISCOVERY_ENABLED_PROPERTY_NAME = "discovery.inmem.enabled";

    private static Logger LOG = Logger.getLogger(InMemDiscoveryFactory.class.getName());

    @Override
    public Discovery build(Properties properties) {
        Discovery discovery = null;
        try {
            if (Boolean.valueOf(properties.getProperty(IN_MEM_DISCOVERY_ENABLED_PROPERTY_NAME))) {
                discovery = new InMemDiscovery();
            }
        } catch (Exception e) {
            LOG.log(Level.INFO, "Error creating InMemDiscovery", e);
        }
        return discovery;
    }

    @Override
    public void setAutoConfiguration(AutoConfiguration ignored) {

    }

}
