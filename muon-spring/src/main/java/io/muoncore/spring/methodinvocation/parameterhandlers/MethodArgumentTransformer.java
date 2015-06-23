package io.muoncore.spring.methodinvocation.parameterhandlers;

public interface MethodArgumentTransformer {
    Object extractArgument(Object muonResourceEvent);

    Class<?> getParameterType();
}
