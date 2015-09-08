package io.muoncore.spring.repository;

import io.muoncore.Muon;
import io.muoncore.MuonClient;
import io.muoncore.exception.MuonException;
import io.muoncore.future.MuonFuture;
import io.muoncore.transport.resource.MuonResourceEvent;
import io.muoncore.transport.resource.MuonResourceEventBuilder;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractRepositoryMethodHandler implements RepositoryMethodHandler {

    public static final String CONFIGURATION_EXCEPTION_TEXT = "Repository method should contain either multiple @Parameter annotated methods, or single object method";
    protected final Class<?> returnType;
    protected final Muon muon;

    protected String muonUrl;
    private Method method;
    protected boolean keepMuonFuture;

    private List<String> parameterNames = new ArrayList<>();

    private QueryMethodParameterCombinationType queryMethodParameterCombinationType;
    protected int queryTimeout;
    protected TimeUnit timeoutUnit;
    protected StringValueResolver valueResolver;

    public AbstractRepositoryMethodHandler(Method method, Muon muon) {
        this.method = method;
        this.muon = muon;
        initParameterHandlers();
        returnType = initReturnType(method);
    }

    private Class<?> initReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        if (MuonFuture.class.isAssignableFrom(returnType)) {
            keepMuonFuture = true;
            returnType = Object.class;
            Type returnGenericType = method.getGenericReturnType();
            if (returnGenericType instanceof ParameterizedType) {
                Type[] returnTypes = ((ParameterizedType) returnGenericType).getActualTypeArguments();
                if (returnTypes != null && returnTypes.length > 0) {
                    returnType = (Class<?>) returnTypes[0];
                }
            }
        }
        return returnType;
    }

    private void initParameterHandlers() {
        for (Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(io.muoncore.spring.annotations.parameterhandlers.Parameter.class)) {
                registerParameterName(parameter.getAnnotation(io.muoncore.spring.annotations.parameterhandlers.Parameter.class).value());
            } else {
                registerObjectQueryType();
            }
        }
        if (queryMethodParameterCombinationType == null) {
            queryMethodParameterCombinationType = QueryMethodParameterCombinationType.EMPTY;
        }
    }

    private void registerObjectQueryType() {
        if (queryMethodParameterCombinationType != null) {
            throw new MuonException(CONFIGURATION_EXCEPTION_TEXT);
        }
        queryMethodParameterCombinationType = QueryMethodParameterCombinationType.OBJECT;
    }

    private void registerParameterName(String value) {
        if (queryMethodParameterCombinationType != null && queryMethodParameterCombinationType != QueryMethodParameterCombinationType.PARAMETERS) {
            throw new MuonException(CONFIGURATION_EXCEPTION_TEXT);
        }
        queryMethodParameterCombinationType = QueryMethodParameterCombinationType.PARAMETERS;
        parameterNames.add(value);
    }

    @Override
    public Object processRepositoryRequest(Object[] args) {
        switch (queryMethodParameterCombinationType) {
            case PARAMETERS:
                return processParametersQuery(args);
            case OBJECT:
                return processObjectQuery(args);
            case EMPTY:
                return processEmptyQuery(args);
        }
        throw new IllegalStateException("Wrong Query method type");
    }

    private Object processEmptyQuery(Object[] args) {
        MuonResourceEvent event = MuonResourceEventBuilder
                .event(null)
                .withUri(muonUrl)
                .build();
        return processMuonOperation(event);
    }

    private Object processMuonOperation(MuonResourceEvent event) {
        MuonFuture<MuonClient.MuonResult> future = executeMuonOperation(event, returnType);
        try {
            if (keepMuonFuture) {
                return future;
            } else {
                return future.get(queryTimeout, timeoutUnit).getResponseEvent().getDecodedContent();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new MuonException("Error performing query " + event, e);
        }
    }

    protected abstract MuonFuture executeMuonOperation(MuonResourceEvent event, Class<?> returnType);

    private Object processObjectQuery(Object[] args) {
        MuonResourceEvent event = MuonResourceEventBuilder
                .event(args[0])
                .withUri(muonUrl)
                .build();
        return processMuonOperation(event);
    }

    private Object processParametersQuery(Object[] args) {
        Map<String, Object> parametersMap = new HashMap<>();
        for (int i = 0; i < parameterNames.size(); i++) {
            parametersMap.put(parameterNames.get(i), args[i]);
        }
        MuonResourceEvent event = MuonResourceEventBuilder
                .event(parametersMap)
                .withUri(muonUrl)
                .build();
        return processMuonOperation(event);
    }

    private enum QueryMethodParameterCombinationType {PARAMETERS, OBJECT, EMPTY}
}
