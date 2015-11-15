package io.muoncore.spring.transport;

import io.muoncore.spring.annotations.MuonRepository;
import io.muoncore.spring.repository.MuonRepositoryFactoryBean;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.MuonTransportFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.*;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Properties;

public class MuonTransportFactoryBeanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    public static final String MUON_PREFIX = "muon.";
    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(MuonTransportFactory.class));
        Properties properties = populateConnectionProperties();
        scanner
                .findCandidateComponents("io.muoncore")
                .stream()
                .forEach(candidateComponent -> {
                    final String beanClassName = candidateComponent.getBeanClassName();

                    BeanDefinitionBuilder definition = BeanDefinitionBuilder
                            .genericBeanDefinition(MuonTransportFactoryBean.class);
                    definition.addPropertyValue("type", beanClassName);
                    definition.addPropertyValue("properties", properties);

                    String beanName = BeanDefinitionReaderUtils.generateBeanName(definition.getBeanDefinition(), registry);

                    final BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(definition.getBeanDefinition(), beanName);

                    BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, registry);
                });
    }

    private Properties populateConnectionProperties() {
        final ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) this.environment;
        Properties properties = new Properties();
        for (PropertySource<?> propertySource : configurableEnvironment.getPropertySources()) {
            if (propertySource instanceof MapPropertySource) {
                ((MapPropertySource) propertySource).getSource().entrySet()
                        .stream()
                        .filter(property -> isMuonTransportProperty(property.getKey()))
                        .forEach(property -> properties.put(property.getKey().replace(MUON_PREFIX, ""), property.getValue()));
            }
        }
        return properties;
    }

    private boolean isMuonTransportProperty(String key) {
        return key.startsWith(MUON_PREFIX);
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
