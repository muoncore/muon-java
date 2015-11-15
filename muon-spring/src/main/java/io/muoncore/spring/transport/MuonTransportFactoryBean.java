package io.muoncore.spring.transport;

import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.MuonTransportFactory;
import org.springframework.beans.factory.FactoryBean;

import java.util.Properties;

public class MuonTransportFactoryBean implements FactoryBean<MuonTransport> {

    private Class<? extends MuonTransportFactory> type;
    private Properties properties;

    @Override
    public MuonTransport getObject() throws Exception {
        MuonTransportFactory factory = type.newInstance();
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
