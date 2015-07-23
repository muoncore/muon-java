package io.muoncore.spring.annotationprocessor;

import io.muoncore.spring.annotations.MuonQueryListener;
import io.muoncore.spring.mapping.MuonResourceService;
import io.muoncore.spring.methodinvocation.MuonResourceMethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

public class MuonResourceListenerMethodAnnotationProcessor implements MethodAnnotationProcessor {

    @Autowired
    private MuonResourceService muonResourceService;

    @Override
    public void processMethod(Method method, Object bean) {
        MuonQueryListener muonQueryListener = AnnotationUtils.findAnnotation(method, MuonQueryListener.class);
        if (muonQueryListener != null) {
            muonResourceService.addQueryMapping(muonQueryListener.path(), new MuonResourceMethodInvocation(method, bean));
        }
    }
}
