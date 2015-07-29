package io.muoncore.spring.annotationprocessor;

import java.lang.reflect.Method;

public interface MethodAnnotationProcessor {
    void processMethod(Method method, Object bean);
}
