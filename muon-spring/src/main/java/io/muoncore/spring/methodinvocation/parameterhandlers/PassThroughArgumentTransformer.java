package io.muoncore.spring.methodinvocation.parameterhandlers;

import java.lang.reflect.Parameter;

public class PassThroughArgumentTransformer implements MethodArgumentTransformer {
    private Parameter parameter;

    public PassThroughArgumentTransformer(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public Object extractArgument(Object muonResourceEvent) {
        return muonResourceEvent;
    }

    @Override
    public Class<?> getParameterType() {
        return parameter.getType();
    }
}
