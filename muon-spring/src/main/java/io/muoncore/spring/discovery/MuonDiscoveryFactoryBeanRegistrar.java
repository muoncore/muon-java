package io.muoncore.spring.discovery;

import io.muoncore.discovery.DiscoveryFactory;
import io.muoncore.spring.PropertiesHelper;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.Properties;

public class MuonDiscoveryFactoryBeanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    public static final String MUON_PREFIX = "muon.";
    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        try {
            //TODO, discovery wait is not working as expected here. block until it's ready.
            //Remove after demos
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(DiscoveryFactory.class));
        Properties properties = PropertiesHelper.populateConnectionProperties(environment, MUON_PREFIX);
        scanner
                .findCandidateComponents("io.muoncore.discovery")
                .stream()
                .forEach(candidateComponent -> {
                    final String beanClassName = candidateComponent.getBeanClassName();

                    BeanDefinitionBuilder definition = BeanDefinitionBuilder
                            .genericBeanDefinition(MuonDiscoveryFactoryBean.class);
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
