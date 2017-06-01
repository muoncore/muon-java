package io.muoncore.spring.methodinvocation.parameterhandlers;

import io.muoncore.protocol.rpc.client.requestresponse.Request;
import io.muoncore.protocol.rpc.client.requestresponse.server.RequestWrapper;

import java.lang.reflect.Parameter;

public class ResourceMethodArgumentTransformerFactory {
    public static MethodArgumentTransformer createMethodParameterHandler(Parameter parameter) {
        if (RequestWrapper.class.equals(parameter.getType())) {
            return new PassThroughArgumentTransformer(parameter);
        } if (Request.class.equals(parameter.getType())) {
            return new RequestArgumentTransformer(parameter);
        } else if (parameter.isAnnotationPresent(io.muoncore.spring.annotations.parameterhandlers.Parameter.class)) {
            return new ParameterEventArgumentTransformer(parameter);
        } else {
            return new PayloadArgumentTransformer(parameter);
        }
    }
}
