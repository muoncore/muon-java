package io.muoncore.spring.discovery;

import io.muoncore.Discovery;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.discovery.DiscoveryFactory;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.MuonTransportFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Properties;

public class MuonDiscoveryFactoryBean implements FactoryBean<Discovery> {

    private Class<? extends DiscoveryFactory> type;
    private Properties properties;

    @Autowired
    private AutoConfiguration autoConfiguration;

    @Override
    public Discovery getObject() throws Exception {
        DiscoveryFactory factory = type.newInstance();
        factory.setAutoConfiguration(autoConfiguration);
        return factory.build(properties);
    }

    @Override
    public Class<?> getObjectType() {
        return Discovery.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setType(Class<? extends DiscoveryFactory> type) {
        this.type = type;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
