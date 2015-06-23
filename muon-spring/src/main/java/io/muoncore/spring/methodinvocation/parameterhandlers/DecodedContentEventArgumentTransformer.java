package io.muoncore.spring.methodinvocation.parameterhandlers;

import io.muoncore.transport.resource.MuonResourceEvent;

import java.lang.reflect.Parameter;

public class DecodedContentEventArgumentTransformer implements MethodArgumentTransformer {
    private Parameter parameter;

    public DecodedContentEventArgumentTransformer(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public Class<?> getParameterType() {
        return parameter.getType();
    }

    @Override
    public Object extractArgument(Object muonResourceEvent) {
        if (muonResourceEvent instanceof MuonResourceEvent) {
            return ((MuonResourceEvent) muonResourceEvent).getDecodedContent();
        } else {
            throw new IllegalStateException("@DecodedContent annotation should be used only on resource handlers");
        }
    }
}
