package io.muoncore.spring.methodinvocation;

import io.muoncore.spring.methodinvocation.parameterhandlers.DecodedContentEventArgumentTransformer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Created by volod on 9/8/2015.
 */
public class MuonCommandMethodInvocation extends AbstractMuonMethodInvocation<Object> {
    public MuonCommandMethodInvocation(Method method, Object bean) {
        super(bean, method);
        initArgumentTransformers();
    }

    private void initArgumentTransformers() {
        if (method.getParameterCount() > 1) {
            throw new IllegalStateException("Muon command listeners can accept only one command object");
        }
        for (Parameter parameter : method.getParameters()) {
            argumentTransformers.add(new DecodedContentEventArgumentTransformer(parameter));
        }
    }

    public Class getDecodedParameterType() {
        if (argumentTransformers.size() > 0) {
            return argumentTransformers.get(0).getParameterType();
        } else {
            return Object.class;
        }
    }

}
