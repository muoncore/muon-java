package io.muoncore.spring.methodinvocation.parameterhandlers;

import io.muoncore.transport.resource.MuonResourceEvent;

import java.lang.reflect.Parameter;

public class MuonHeadersEventArgumentTransformer implements MethodArgumentTransformer {
    private Parameter parameter;

    public MuonHeadersEventArgumentTransformer(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public Class<?> getParameterType() {
        return parameter.getType();
    }

    @Override
    public Object extractArgument(Object muonEvent) {
        if (muonEvent instanceof MuonResourceEvent) {
            return ((MuonResourceEvent) muonEvent).getHeaders();
        } else {
            throw new IllegalStateException("@MuonHeaders annotation should be used only on resource handlers");
        }
    }
}
