package io.muoncore.spring;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.StringValueResolver;
import reactor.core.support.Assert;

import java.util.ArrayList;
import java.util.List;

public class AutoConfigurationFactoryBean implements FactoryBean<AutoConfiguration>, EmbeddedValueResolverAware {
    private StringValueResolver embeddedValueResolver;

    private String serviceName;
    private String[] tags;
    private String aesEncryptionKey;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.embeddedValueResolver = resolver;
    }

    @Override
    public AutoConfiguration getObject() throws Exception {
        Assert.notNull(serviceName);
        String resolvedServiceName = embeddedValueResolver.resolveStringValue(serviceName);
        List<String> resolvedTags = resolveTags(tags);

        AutoConfiguration config = MuonConfigBuilder
                .withServiceIdentifier(resolvedServiceName)
                .withTags(resolvedTags.toArray(new String[resolvedTags.size()])).build();

        return config;
    }

    private List<String> resolveTags(String[] tags) {
        List<String> result = new ArrayList<>();
        if (tags != null && tags.length > 0) {
            for (String tag : tags) {
                result.add(embeddedValueResolver.resolveStringValue(tag));
            }
        }
        return result;
    }

    @Override
    public Class<?> getObjectType() {
        return AutoConfiguration.class;
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

    public String getAesEncryptionKey() {
        return aesEncryptionKey;
    }

    public void setAesEncryptionKey(String aesEncryptionKey) {
        this.aesEncryptionKey = aesEncryptionKey;
    }
}
