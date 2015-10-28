package io.muoncore.spring.controllers;

import io.muoncore.spring.annotations.EnableMuonControllers;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

public class MuonControllersConfigurationHolderBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    public static final String MUON_CONTROLLERS_CONFIGURATION_HOLDER_BEAN_NAME = "muonControllersConfigurationHolder";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableMuonControllers.class.getName());
        Object keepAliveTimeout = annotationAttributes.get("streamKeepAliveTimeout");
        if (keepAliveTimeout != null && Integer.class == keepAliveTimeout.getClass()) {
            BeanDefinitionBuilder definition = BeanDefinitionBuilder
                    .genericBeanDefinition(MuonControllersConfigurationHolder.class);
            definition.addPropertyValue("streamKeepAliveTimeout", keepAliveTimeout);
            definition.addPropertyValue("timeUnit", annotationAttributes.get("timeUnit"));
            BeanDefinitionReaderUtils.registerBeanDefinition(
                    new BeanDefinitionHolder(definition.getBeanDefinition(), MUON_CONTROLLERS_CONFIGURATION_HOLDER_BEAN_NAME), registry);
        }

    }
}
