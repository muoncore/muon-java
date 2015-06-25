package io.muoncore.spring.methodinvocation.parameterhandlers;

import io.muoncore.spring.annotations.parameterhandlers.MuonHeader;
import io.muoncore.spring.mapping.MuonMappingException;
import io.muoncore.spring.annotations.parameterhandlers.DecodedContent;
import io.muoncore.spring.annotations.parameterhandlers.MuonHeaders;
import io.muoncore.transport.resource.MuonResourceEvent;

import java.lang.reflect.Parameter;

public class ResourceMethodArgumentTransformerFactory {
    public static MethodArgumentTransformer createMethodParameterHandler(Parameter parameter) {
        if (MuonResourceEvent.class.equals(parameter.getType())) {
            return new PassThroughArgumentTransformer(parameter);
        } else if (parameter.isAnnotationPresent(DecodedContent.class)) {
            return new DecodedContentEventArgumentTransformer(parameter);
        } else if (parameter.isAnnotationPresent(MuonHeaders.class)) {
            return new MuonHeadersEventArgumentTransformer(parameter);
        } else if (parameter.isAnnotationPresent(MuonHeader.class)) {
            return new MuonSingleHeaderEventArgumentTransformer(parameter);
        } else {
            throw new MuonMappingException("Unsupported argument type found");
        }
    }
}
