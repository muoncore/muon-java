package io.muoncore.spring.repository;

import io.muoncore.Muon;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Proxy;

public class MuonRepositoryFactoryBean implements FactoryBean<Object>, EmbeddedValueResolverAware {

    private Class<?> type;

    @Autowired
    private Muon muon;
    private StringValueResolver valueResolver;

    @Override
    public Object getObject() throws Exception {
        MuonRepositoryInvocationHandler invocationHandler = new MuonRepositoryInvocationHandler(this.type, muon, valueResolver);
        return Proxy
                .newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, invocationHandler);
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.valueResolver = resolver;
    }
}
