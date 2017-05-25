package io.muoncore.spring.repository;

import io.muoncore.Muon;
import io.muoncore.exception.MuonException;
import io.muoncore.api.MuonFuture;
import io.muoncore.protocol.rpc.client.requestresponse.Response;
import io.muoncore.spring.annotations.Request;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RequestRepositoryMethodHandler implements RepositoryMethodHandler {

    public static final String CONFIGURATION_EXCEPTION_TEXT = "Repository method should contain either multiple @Parameter annotated methods, or single object method";
    protected final Type returnType;
    protected final Muon muon;

    protected String muonUrl;
    private Method method;
    protected boolean keepMuonFuture;

    private List<String> parameterNames = new ArrayList<>();

    private QueryMethodParameterCombinationType queryMethodParameterCombinationType;
    protected int queryTimeout;
    protected TimeUnit timeoutUnit;

    public RequestRepositoryMethodHandler(Method method, Muon muon, StringValueResolver resolver) {
        this.method = method;
        this.muon = muon;
        initParameterHandlers();
        returnType = initReturnType(method);
        Request queryAnnotation = method.getAnnotation(Request.class);
        this.queryTimeout = queryAnnotation.timeout();
        this.timeoutUnit = queryAnnotation.timeUnit();
        this.muonUrl = resolver.resolveStringValue(queryAnnotation.value());

    }


    private Type initReturnType(Method method) {

        Type returnType = method.getGenericReturnType();
        if (returnType == void.class) {
            returnType =  Object.class;
        }
//TODO Implement MuonFuture pass-through here
/*
        if (MuonFuture.class.isAssignableFrom(returnType)) {
            keepMuonFuture = true;
            returnType = method.getReturnType();
            Type returnGenericType = method.getGenericReturnType();
            if (returnGenericType instanceof ParameterizedType) {
                Type[] returnTypes = ((ParameterizedType) returnGenericType).getActualTypeArguments();
                if (returnTypes != null && returnTypes.length > 0) {
                    returnType = (Class<?>) returnTypes[0];
                }
            }
        }
*/
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
                return processEmptyQuery();
        }
        throw new IllegalStateException("Wrong Query method type");
    }

    private Object processEmptyQuery() {
        return processMuonOperation(null);
    }

    private Object processMuonOperation(Object payload) {
        try {
            MuonFuture<Response> future = executeMuonOperation(payload);
            if (keepMuonFuture) {
                return future;
            } else {
                return future.get(queryTimeout, timeoutUnit).getPayload(Object.class);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException | URISyntaxException e) {
            throw new MuonException("Error performing query " + muonUrl + "\nParameters: " + payload
                    , e);
        }
    }

    protected MuonFuture<Response> executeMuonOperation(Object payload) throws URISyntaxException {
        return muon.request(muonUrl, payload);
    }

    private Object processObjectQuery(Object[] args) {
        return processMuonOperation(args[0]);
    }

    private Object processParametersQuery(Object[] args) {
        Map<String, Object> parametersMap = new HashMap<>();
        for (int i = 0; i < parameterNames.size(); i++) {
            parametersMap.put(parameterNames.get(i), args[i]);
        }
        return processMuonOperation(parametersMap);
    }

    private enum QueryMethodParameterCombinationType {PARAMETERS, OBJECT, EMPTY}

}
