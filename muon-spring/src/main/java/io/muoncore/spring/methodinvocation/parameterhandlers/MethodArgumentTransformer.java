package io.muoncore.spring.methodinvocation.parameterhandlers;

import java.lang.reflect.Type;

public interface MethodArgumentTransformer {
    Object extractArgument(Object request);

    Type getParameterType();
}
