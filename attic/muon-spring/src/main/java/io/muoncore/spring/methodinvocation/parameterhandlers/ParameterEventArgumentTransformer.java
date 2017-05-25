package io.muoncore.spring.methodinvocation.parameterhandlers;


import io.muoncore.protocol.rpc.client.requestresponse.server.RequestWrapper;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;

public class ParameterEventArgumentTransformer implements MethodArgumentTransformer {
    private Parameter parameter;
    private String parameterName;

    public ParameterEventArgumentTransformer(Parameter parameter) {
        this.parameter = parameter;
        final io.muoncore.spring.annotations.parameterhandlers.Parameter muonParameterAnnotation = parameter.getAnnotation(io.muoncore.spring.annotations.parameterhandlers.Parameter.class);
        assert muonParameterAnnotation != null;
        parameterName = muonParameterAnnotation.value();
    }

    @Override
    public Type getParameterType() {
        return parameter.getParameterizedType();
    }

    @Override
    public Object extractArgument(Object request) {
        if (request instanceof RequestWrapper) {
            Object decodedContent = ((RequestWrapper) request).getRequest().getPayload(Object.class);
            if (decodedContent instanceof Map) {
                Object result = ((Map) decodedContent).get(parameterName);
                return castNumericTypes(result);
            }
        }
        throw new IllegalStateException("@Parameter(\"<parameter-name>\") annotation should be used only on resource handlers");
    }

    private Object castNumericTypes(Object result) {
        if (!(result instanceof Number)) {
            return result;
        }
        Number number = (Number) result;
        if (Long.class.equals(parameter.getType()) || long.class.equals(parameter.getType())) {
            return number.longValue();
        }
        if (Integer.class.equals(parameter.getType()) || int.class.equals(parameter.getType())) {
            return number.intValue();
        }
        if (Byte.class.equals(parameter.getType()) || byte.class.equals(parameter.getType())) {
            return number.byteValue();
        }
        if (Float.class.equals(parameter.getType()) || float.class.equals(parameter.getType())) {
            return number.floatValue();
        }
        if (Short.class.equals(parameter.getType()) || short.class.equals(parameter.getType())) {
            return number.shortValue();
        }
        if (Double.class.equals(parameter.getType()) || double.class.equals(parameter.getType())) {
            return number.doubleValue();
        }
        return number;
    }
}
