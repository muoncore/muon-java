package io.muoncore.spring;

import io.muoncore.spring.annotationprocessor.MethodAnnotationProcessor;
import io.muoncore.spring.annotations.MuonController;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.List;

public class MuonControllerBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    private List<MethodAnnotationProcessor> methodAnnotationProcessors;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean == null) {
            return null;
        }
        Class beanClazz = bean.getClass();
        MuonController annotation = AnnotationUtils.findAnnotation(beanClazz, MuonController.class);
        if (annotation == null) {
            return bean;
        }
        for (Method method : beanClazz.getMethods()) {
            for (MethodAnnotationProcessor methodAnnotationProcessor : methodAnnotationProcessors) {
                methodAnnotationProcessor.processMethod(method, bean);
            }
        }
        return bean;
    }

}
