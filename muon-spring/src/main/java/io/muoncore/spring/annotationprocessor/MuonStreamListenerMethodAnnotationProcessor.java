package io.muoncore.spring.annotationprocessor;

import io.muoncore.spring.annotations.MuonStreamListener;
import io.muoncore.spring.mapping.MuonStreamSubscriptionService;
import io.muoncore.spring.methodinvocation.MuonStreamMethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

public class MuonStreamListenerMethodAnnotationProcessor implements MethodAnnotationProcessor {
    @Autowired
    private MuonStreamSubscriptionService streamSubscrioptionService;

    @Override
    public void processMethod(Method method, Object bean) {
        MuonStreamListener muonStreamListener = AnnotationUtils.findAnnotation(method, MuonStreamListener.class);
        if (muonStreamListener != null) {
            streamSubscrioptionService.setupMuonMapping(muonStreamListener.url(), new MuonStreamMethodInvocation(method, bean));
        }

    }
}
