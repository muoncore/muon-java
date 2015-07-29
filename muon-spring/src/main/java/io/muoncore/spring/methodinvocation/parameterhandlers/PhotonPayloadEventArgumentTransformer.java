package io.muoncore.spring.methodinvocation.parameterhandlers;

import io.muoncore.exception.MuonException;
import io.muoncore.spring.photon.PhotonEvent;

import java.lang.reflect.Parameter;

public class PhotonPayloadEventArgumentTransformer implements MethodArgumentTransformer {
    public PhotonPayloadEventArgumentTransformer(Parameter ignored) {
    }

    @Override
    public Class<?> getParameterType() {
        return PhotonEvent.class;
    }

    @Override
    public Object extractArgument(Object muonEvent) {
        if (muonEvent instanceof PhotonEvent) {
            return ((PhotonEvent) muonEvent).getPayload();
        } else {
            throw new MuonException("@PhotonPayload should be used only on photon streams");
        }
    }
}
