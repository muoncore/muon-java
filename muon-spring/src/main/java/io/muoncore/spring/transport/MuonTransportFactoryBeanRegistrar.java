package io.muoncore.spring.transport;

import io.muoncore.spring.PropertiesHelper;
import io.muoncore.transport.MuonTransportFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.*;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.Properties;

public class MuonTransportFactoryBeanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    public static final String MUON_PREFIX = "muon.";
    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(MuonTransportFactory.class));
        Properties properties = PropertiesHelper.populateConnectionProperties(environment, MUON_PREFIX);
        scanner
                .findCandidateComponents("io.muoncore.transport")
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

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
