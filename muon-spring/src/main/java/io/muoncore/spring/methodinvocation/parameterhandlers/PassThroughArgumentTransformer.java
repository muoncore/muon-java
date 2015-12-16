package io.muoncore.spring.methodinvocation.parameterhandlers;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public class PassThroughArgumentTransformer implements MethodArgumentTransformer {
    private Parameter parameter;

    public PassThroughArgumentTransformer(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public Object extractArgument(Object request) {
        return request;
    }

    @Override
    public Type getParameterType() {
        return parameter.getParameterizedType();
    }
}
