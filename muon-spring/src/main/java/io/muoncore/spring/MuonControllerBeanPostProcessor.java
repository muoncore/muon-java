package io.muoncore.spring;

import io.muoncore.spring.annotations.MuonController;
import io.muoncore.spring.annotations.MuonRequestListener;
import io.muoncore.spring.annotations.MuonStreamListener;
import io.muoncore.spring.mapping.MuonRequestListenerService;
import io.muoncore.spring.mapping.MuonStreamSubscriptionService;
import io.muoncore.spring.methodinvocation.MuonRequestMethodInvocation;
import io.muoncore.spring.methodinvocation.MuonStreamMethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;

public class MuonControllerBeanPostProcessor implements BeanPostProcessor, EmbeddedValueResolverAware {
    @Autowired
    private MuonStreamSubscriptionService streamSubscrioptionService;
    @Autowired
    private MuonRequestListenerService muonRequestListenerService;
    private StringValueResolver resolver;

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
            MuonStreamListener muonStreamListener = AnnotationUtils.findAnnotation(method, MuonStreamListener.class);
            if (muonStreamListener != null) {
                streamSubscrioptionService.setupMuonMapping(resolver.resolveStringValue(muonStreamListener.url()), new MuonStreamMethodInvocation(method, bean));
            }
            MuonRequestListener muonQueryListener = AnnotationUtils.findAnnotation(method, MuonRequestListener.class);
            if (muonQueryListener != null) {
                muonRequestListenerService.addRequestMapping(resolver.resolveStringValue(muonQueryListener.path()), new MuonRequestMethodInvocation(method, bean));
            }
        }
        return bean;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.resolver = resolver;
    }
}
