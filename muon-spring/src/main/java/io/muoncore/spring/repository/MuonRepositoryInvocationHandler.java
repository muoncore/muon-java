package io.muoncore.spring.repository;

import io.muoncore.Muon;
import io.muoncore.spring.annotations.Request;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class MuonRepositoryInvocationHandler implements InvocationHandler {

    private Class<?> type;

    private Map<Method, RepositoryMethodHandler> methodHandlers = new HashMap<>();

    public MuonRepositoryInvocationHandler(Class<?> type, Muon muon, StringValueResolver resolver) {
        this.type = type;
        for (Method method : type.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Request.class)) {
                methodHandlers.put(method, new RequestRepositoryMethodHandler(method, muon, resolver));
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("equals".equals(method.getName())) {
            Object otherHandler =
                    args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
            return equals(otherHandler);
        } else if ("hashCode".equals(method.getName())) {
            return hashCode();
        } else if ("toString".equals(method.getName())) {
            return toString();
        }
        RepositoryMethodHandler handler = methodHandlers.get(method);
        if (handler != null) {
            return handler.processRepositoryRequest(args);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return type.getName() + "@" + Integer.toHexString(hashCode()) + "(proxied by MuonRepositoryInvocationHandler)";
    }

}
