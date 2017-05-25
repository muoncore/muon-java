package io.muoncore.spring.methodinvocation.parameterhandlers;

import io.muoncore.protocol.rpc.client.requestresponse.server.RequestWrapper;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;

public class PayloadArgumentTransformer implements MethodArgumentTransformer {
    private Parameter parameter;

    public PayloadArgumentTransformer(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public Type getParameterType() {
        return parameter.getParameterizedType();
    }

    @Override
    public Object extractArgument(Object muonRequest) {
        if (muonRequest instanceof RequestWrapper) {
            return ((RequestWrapper) muonRequest).getRequest().getPayload(Map.class);
        } else {
            throw new IllegalStateException("@DecodedContent annotation should be used only on resource handlers");
        }
    }
}
