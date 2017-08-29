package io.muoncore.transport;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.memory.transport.InMemTransport;
import io.muoncore.memory.transport.bus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class InMemTransportFactory implements MuonTransportFactory {

    private static final String IN_MEM_TRANSPORT_ENABLED_PROPERTY_NAME = "transport.inmem.enabled";
    public static EventBus EVENT_BUS = new EventBus();

    private static Logger LOG = LoggerFactory.getLogger(InMemTransportFactory.class.getName());
    private AutoConfiguration autoConfiguration;

    @Override
    public MuonTransport build(Properties properties) {
        MuonTransport transport = null;
        try {
//            if (Boolean.valueOf(properties.getProperty(IN_MEM_TRANSPORT_ENABLED_PROPERTY_NAME))) {
                transport = new InMemTransport(autoConfiguration, getSharedEventBus());
//            }
        } catch (Exception e) {
            LOG.info("Error creating InMemTransport", e);
        }
        return transport;
    }

    private EventBus getSharedEventBus() {
        return EVENT_BUS;
    }

    @Override
    public void setAutoConfiguration(AutoConfiguration autoConfiguration) {
        this.autoConfiguration = autoConfiguration;
    }

}
