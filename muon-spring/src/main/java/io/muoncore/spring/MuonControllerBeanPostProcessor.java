package io.muoncore.spring;

import io.muoncore.spring.annotations.MuonController;
import io.muoncore.spring.annotations.MuonQueryListener;
import io.muoncore.spring.annotations.MuonStreamListener;
import io.muoncore.spring.mapping.MuonResourceService;
import io.muoncore.spring.mapping.MuonStreamSubscriptionService;
import io.muoncore.spring.methodinvocation.MuonResourceMethodInvocation;
import io.muoncore.spring.methodinvocation.MuonStreamMethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

public class MuonControllerBeanPostProcessor implements BeanPostProcessor {
    @Autowired
    private MuonStreamSubscriptionService streamSubscrioptionService;
    @Autowired
    private MuonResourceService muonResourceService;

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
                streamSubscrioptionService.setupMuonMapping(muonStreamListener.url(), new MuonStreamMethodInvocation(method, bean));
            }
            MuonQueryListener muonQueryListener = AnnotationUtils.findAnnotation(method, MuonQueryListener.class);
            if (muonQueryListener != null) {
                muonResourceService.addQueryMapping(muonQueryListener.path(), new MuonResourceMethodInvocation(method, bean));
            }

        }
        return bean;
    }

}
