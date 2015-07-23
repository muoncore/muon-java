package io.muoncore.spring.methodinvocation.parameterhandlers;

import io.muoncore.spring.annotations.parameterhandlers.MuonHeader;
import io.muoncore.transport.resource.MuonResourceEvent;

import java.lang.reflect.Parameter;

public class MuonSingleHeaderEventArgumentTransformer implements MethodArgumentTransformer {
    private Parameter parameter;

    public MuonSingleHeaderEventArgumentTransformer(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public Class<?> getParameterType() {
        return parameter.getType();
    }

    @Override
    public Object extractArgument(Object muonEvent) {
        if (muonEvent instanceof MuonResourceEvent) {
            final MuonHeader muonHeaderAnnotation = parameter.getAnnotation(MuonHeader.class);
            assert muonHeaderAnnotation != null;
            return ((MuonResourceEvent) muonEvent).getHeaders().get(muonHeaderAnnotation.value());
        } else {
            throw new IllegalStateException("@MuonHeader(\"<header-name>\") annotation should be used only on resource handlers");
        }
    }
}
