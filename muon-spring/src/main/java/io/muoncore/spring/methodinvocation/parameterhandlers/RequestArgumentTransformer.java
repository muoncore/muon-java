package io.muoncore.spring.methodinvocation.parameterhandlers;

import io.muoncore.protocol.requestresponse.server.RequestWrapper;

import java.lang.reflect.Parameter;

public class RequestArgumentTransformer implements MethodArgumentTransformer {
    private final Parameter parameter;

    public RequestArgumentTransformer(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public Object extractArgument(Object request) {
        assert request instanceof RequestWrapper;
        return ((RequestWrapper) request).getRequest();
    }

    @Override
    public Class<?> getParameterType() {
        return parameter.getType();
    }
}
