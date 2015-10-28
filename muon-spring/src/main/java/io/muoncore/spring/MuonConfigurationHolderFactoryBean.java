package io.muoncore.spring;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.StringValueResolver;
import reactor.core.support.Assert;

public class MuonConfigurationHolderFactoryBean implements FactoryBean<MuonConfigurationHolder>, EmbeddedValueResolverAware {
    private StringValueResolver embeddedValueResolver;

    private String serviceName;
    private String[] tags;
    private String discoveryUrl;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.embeddedValueResolver = resolver;
    }

    @Override
    public MuonConfigurationHolder getObject() throws Exception {
        Assert.notNull(serviceName);
        String resolvedServiceName = embeddedValueResolver.resolveStringValue(serviceName);
        String resolvedTags[] = resolveTags(tags);
        String resolvedDiscoveryUrl = embeddedValueResolver.resolveStringValue(discoveryUrl);

        return new MuonConfigurationHolder(resolvedServiceName, resolvedTags, resolvedDiscoveryUrl);
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
        return MuonConfigurationHolder.class;
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
