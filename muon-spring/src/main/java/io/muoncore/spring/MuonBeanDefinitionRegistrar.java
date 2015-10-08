package io.muoncore.spring;

import io.muoncore.spring.annotations.EnableMuon;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

public class MuonBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    public static final String MUON_BEAN_NAME = "muon";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableMuon.class.getName());
        Object serviceNameCandidate = annotationAttributes.get("serviceName");
        if (serviceNameCandidate != null && String.class == serviceNameCandidate.getClass()) {
            String serviceName = (String) serviceNameCandidate;
            BeanDefinitionBuilder definition = BeanDefinitionBuilder
                    .genericBeanDefinition(MuonFactoryBean.class);
            definition.addPropertyValue("serviceName", serviceName);
            definition.addPropertyValue("tags", annotationAttributes.get("tags"));
            definition.addPropertyValue("discoveryUrl", annotationAttributes.get("discoveryUrl"));
            BeanDefinitionReaderUtils.registerBeanDefinition(
                    new BeanDefinitionHolder(definition.getBeanDefinition(), MUON_BEAN_NAME), registry);
        }

    }
}
