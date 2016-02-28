package io.muoncore.spring;

import io.muoncore.spring.annotations.EnableMuon;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

public class AutoConfigurationBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    public static final String AUTO_CONFIGURATION_BEAN_NAME = "muonAutoConfiguration";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableMuon.class.getName());


        Object serviceNameCandidate = annotationAttributes.get("serviceName");
        if (serviceNameCandidate != null && String.class == serviceNameCandidate.getClass()) {
            String serviceName = (String) serviceNameCandidate;
            BeanDefinitionBuilder definition = BeanDefinitionBuilder
                    .genericBeanDefinition(AutoConfigurationFactoryBean.class);
            definition.addPropertyValue("serviceName", serviceName);
            definition.addPropertyValue("tags", annotationAttributes.get("tags"));
            BeanDefinitionReaderUtils.registerBeanDefinition(
                    new BeanDefinitionHolder(definition.getBeanDefinition(), AUTO_CONFIGURATION_BEAN_NAME), registry);
        }
    }
}
