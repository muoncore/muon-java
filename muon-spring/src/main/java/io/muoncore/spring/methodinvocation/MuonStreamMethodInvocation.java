package io.muoncore.spring.methodinvocation;

import io.muoncore.spring.methodinvocation.parameterhandlers.StreamMethodArgumentTransformerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MuonStreamMethodInvocation extends AbstractMuonMethodInvocation<Object> {

    public MuonStreamMethodInvocation(Method method, Object bean) {
        super(bean, method);
        initArgumentTransformers();
    }

    private void initArgumentTransformers() {
        if (method.getParameterCount() > 1) {
            throw new IllegalStateException("Streaming subscription handles should have only one parameter, event object");
        }
        for (Parameter parameter : method.getParameters()) {
            argumentTransformers.add(
                    StreamMethodArgumentTransformerFactory.createMethodParameterHandler(parameter)
            );
        }
    }

    public Class getDecodedParameterType() {
        if (argumentTransformers.get(0) != null) {
            return argumentTransformers.get(0).getParameterType();
        } else {
            return Object.class;
        }
    }
}
