package io.muoncore.spring.methodinvocation;

import io.muoncore.protocol.requestresponse.server.RequestWrapper;
import io.muoncore.spring.methodinvocation.parameterhandlers.DecodedContentEventArgumentTransformer;
import io.muoncore.spring.methodinvocation.parameterhandlers.MethodArgumentTransformer;
import io.muoncore.spring.methodinvocation.parameterhandlers.ResourceMethodArgumentTransformerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MuonRequestMethodInvocation extends AbstractMuonMethodInvocation<RequestWrapper> {

    public MuonRequestMethodInvocation(Method method, Object bean) {
        super(bean, method);
        initArgumentTransformers();
    }

    private void initArgumentTransformers() {
        for (Parameter parameterType : method.getParameters()) {
            argumentTransformers.add(ResourceMethodArgumentTransformerFactory.createMethodParameterHandler(parameterType));
        }
    }

    public Class getDecodedParameterType() {
        for (MethodArgumentTransformer methodArgumentTransformer : argumentTransformers) {
            if (methodArgumentTransformer instanceof DecodedContentEventArgumentTransformer) {
                return methodArgumentTransformer.getParameterType();
            }
        }
        return Object.class;
    }
}
