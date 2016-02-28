package io.muoncore.spring;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringValueResolver;
import reactor.core.support.Assert;

import java.util.ArrayList;
import java.util.List;

public class AutoConfigurationFactoryBean implements FactoryBean<AutoConfiguration>, EmbeddedValueResolverAware {
    private StringValueResolver embeddedValueResolver;

    private String serviceName;
    private String[] tags;
    @Autowired private Environment environment;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.embeddedValueResolver = resolver;
    }

    @Override
    public AutoConfiguration getObject() throws Exception {
        Assert.notNull(serviceName);
        String resolvedServiceName = embeddedValueResolver.resolveStringValue(serviceName);
        List<String> resolvedTags = resolveTags(tags);

        return MuonConfigBuilder
                .withServiceIdentifier(resolvedServiceName)
                .addWriter(autoConfig -> {
                    ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
                    for (PropertySource<?> propertySource : configurableEnvironment.getPropertySources()) {
                        if (propertySource instanceof EnumerablePropertySource) {
                            final EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) propertySource;
                            for (String name : enumerablePropertySource.getPropertyNames()) {
                                autoConfig.getProperties().put(name, enumerablePropertySource.getProperty(name));
                            }
                        }
                    }
                })
                .withTags(resolvedTags.toArray(new String[resolvedTags.size()]))
                .build();
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
}
