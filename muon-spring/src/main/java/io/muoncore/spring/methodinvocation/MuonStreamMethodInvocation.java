package io.muoncore.spring.methodinvocation;

import io.muoncore.spring.methodinvocation.parameterhandlers.PassThroughArgumentTransformer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public class MuonStreamMethodInvocation extends AbstractMuonMethodInvocation<Object> {

    public MuonStreamMethodInvocation(Method method, Object bean) {
        super(bean, method);
        initArgumentTransformers();
    }

    private void initArgumentTransformers() {
        //TODO Shall we support invocations with parameterCount == 0?
        if (method.getParameterCount() > 1) {
            throw new IllegalStateException("Streaming subscription handles should have only one parameter, event object");
        }
        for (Parameter parameter : method.getParameters()) {
            argumentTransformers.add(new PassThroughArgumentTransformer(parameter));
        }
    }

    public Type getDecodedParameterType() {
        if (argumentTransformers.get(0) != null) {
            return argumentTransformers.get(0).getParameterType();
        } else {
            return Object.class;
        }
    }
}
