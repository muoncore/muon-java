package io.muoncore.spring;

import io.muoncore.spring.annotations.EnableMuon;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

public class MuonServiceNameBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    public static final String MUON_ANNOTATION_CONFIG_SERVICE_NAME = "muonAnnotationConfigServiceName";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableMuon.class.getName());
        Object serviceNameCandidate = annotationAttributes.get("serviceName");
        if (serviceNameCandidate != null && String.class == serviceNameCandidate.getClass()) {
            String serviceName = (String) serviceNameCandidate;
            registry.registerBeanDefinition(MUON_ANNOTATION_CONFIG_SERVICE_NAME,
                    BeanDefinitionBuilder.genericBeanDefinition(String.class)
                            .setFactoryMethod("valueOf")
                            .addConstructorArgValue(serviceName)
                            .getBeanDefinition());
        }
    }
}
