package io.muoncore.spring.methodinvocation.parameterhandlers;

public interface MethodArgumentTransformer {
    Object extractArgument(Object request);

    Class<?> getParameterType();
}
