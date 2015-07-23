package io.muoncore.spring.methodinvocation.parameterhandlers;

import io.muoncore.spring.annotations.parameterhandlers.PhotonPayload;

import java.lang.reflect.Parameter;

public class StreamMethodArgumentTransformerFactory {
    public static MethodArgumentTransformer createMethodParameterHandler(Parameter parameter) {
        if (parameter.isAnnotationPresent(PhotonPayload.class)) {
            return new PhotonPayloadEventArgumentTransformer(parameter);
        } else {
            return new PassThroughArgumentTransformer(parameter);
        }
    }
}
