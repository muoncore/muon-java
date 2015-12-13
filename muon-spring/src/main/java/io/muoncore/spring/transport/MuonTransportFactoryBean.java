package io.muoncore.spring.transport;

import io.muoncore.Discovery;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.MuonTransportFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Properties;

public class MuonTransportFactoryBean implements FactoryBean<MuonTransport> {

    private Class<? extends MuonTransportFactory> type;
    private Properties properties;

    @Autowired
    private AutoConfiguration autoConfiguration;

    @Override
    public MuonTransport getObject() throws Exception {
        MuonTransportFactory factory = type.newInstance();
        factory.setAutoConfiguration(autoConfiguration);
        return factory.build(properties);
    }

    @Override
    public Class<?> getObjectType() {
        return MuonTransport.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setType(Class<? extends MuonTransportFactory> type) {
        this.type = type;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
