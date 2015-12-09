package io.muoncore.spring.methodinvocation.parameterhandlers;

import io.muoncore.protocol.requestresponse.server.RequestWrapper;

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
    public Object extractArgument(Object muonRequest) {
        if (muonRequest instanceof RequestWrapper) {
            return ((RequestWrapper) muonRequest).getRequest().getPayload();
        } else {
            throw new IllegalStateException("@DecodedContent annotation should be used only on resource handlers");
        }
    }
}
