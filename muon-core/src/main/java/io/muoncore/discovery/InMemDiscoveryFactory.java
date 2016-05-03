package io.muoncore.discovery;

import io.muoncore.Discovery;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.memory.discovery.InMemDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class InMemDiscoveryFactory implements DiscoveryFactory {

    private static final String IN_MEM_DISCOVERY_ENABLED_PROPERTY_NAME = "discovery.inmem.enabled";
    public static InMemDiscovery INSTANCE = new InMemDiscovery();

    private static Logger LOG = LoggerFactory.getLogger(InMemDiscoveryFactory.class.getName());

    @Override
    public Discovery build(Properties properties) {
        Discovery discovery = null;
        try {
//            if (Boolean.valueOf(properties.getProperty(IN_MEM_DISCOVERY_ENABLED_PROPERTY_NAME))) {
                discovery = getSharedDiscovery();
//            }
        } catch (Exception e) {
            LOG.info("Error creating InMemDiscovery", e);
        }
        return discovery;
    }

    private InMemDiscovery getSharedDiscovery() {
        return INSTANCE;
    }

    @Override
    public void setAutoConfiguration(AutoConfiguration ignored) {

    }

}
