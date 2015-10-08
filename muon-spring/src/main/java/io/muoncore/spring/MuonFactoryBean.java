package io.muoncore.spring;

import io.muoncore.Muon;
import io.muoncore.config.MuonBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.StringValueResolver;
import reactor.core.support.Assert;

public class MuonFactoryBean implements FactoryBean<Muon>, EmbeddedValueResolverAware {
    private StringValueResolver embeddedValueResolver;

    private String serviceName;
    private String[] tags;
    private String discoveryUrl;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.embeddedValueResolver = resolver;
    }

    @Override
    public Muon getObject() throws Exception {
        Assert.notNull(serviceName);
        String resolvedServiceName = embeddedValueResolver.resolveStringValue(serviceName);
        String resolvedTags[] = resolveTags(tags);
        String resolvedDiscoveryUrl = embeddedValueResolver.resolveStringValue(discoveryUrl);
        MuonBuilder.addWriter(new SpringBasedConfigurationWriter(resolvedDiscoveryUrl));
        MuonBuilder muonBuilder = new MuonBuilder()
                .withServiceIdentifier(resolvedServiceName);
        if (resolvedTags != null) {
            muonBuilder.withTags(resolvedTags);
        }
        final Muon muon = muonBuilder
                .build();

        muon.start();

        return muon;
    }

    private String[] resolveTags(String[] tags) {
        String[] result = null;
        if (tags != null && tags.length > 0) {
            result = new String[tags.length];
            for (int i = 0; i < tags.length; i++) {
                result[i] = embeddedValueResolver.resolveStringValue(tags[i]);
            }
        }
        return result;
    }

    @Override
    public Class<?> getObjectType() {
        return Muon.class;
    }


    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public void setDiscoveryUrl(String discoveryUrl) {
        this.discoveryUrl = discoveryUrl;
    }
}
