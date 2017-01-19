package io.muoncore.spring.methodinvocation;

import io.muoncore.spring.mapping.MuonMappingException;
import io.muoncore.spring.methodinvocation.parameterhandlers.MethodArgumentTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

abstract public class AbstractMuonMethodInvocation<T> {
    protected final Method method;
    protected final Object bean;
    final List<MethodArgumentTransformer> argumentTransformers;

    private Logger log = LoggerFactory.getLogger(AbstractMuonMethodInvocation.class.getName());

    public AbstractMuonMethodInvocation(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
        argumentTransformers = new ArrayList<>(method.getParameterCount());
    }

    public Object invoke(T arg) {
        try {
            log.debug("Executing method " + method + " with event " + arg);
            return method.invoke(bean, transformArguments(arg));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new MuonMappingException(e);
        }
    }

    private Object[] transformArguments(T muonResourceEvent) {
        Object[] arguments = new Object[method.getParameterCount()];
        for (int i = 0; i < method.getParameterCount(); i++) {
            arguments[i] = argumentTransformers.get(i).extractArgument(muonResourceEvent);
        }
        return arguments;
    }
}
