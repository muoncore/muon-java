package io.muoncore.spring.repository;

import io.muoncore.Muon;
import io.muoncore.future.MuonFuture;
import io.muoncore.spring.annotations.Query;
import io.muoncore.transport.resource.MuonResourceEvent;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;

public class QueryRepositoryMethodHandler extends AbstractRepositoryMethodHandler {
    public QueryRepositoryMethodHandler(Method method, Muon muon, StringValueResolver resolver) {
        super(method, muon);
        initAnnotationParameters(method.getAnnotation(Query.class));
        if (resolver != null) {
            this.muonUrl = resolver.resolveStringValue(muonUrl);
        }
    }

    @Override
    protected MuonFuture executeMuonOperation(MuonResourceEvent event, Class<?> returnType) {
        return muon.query(event, returnType);
    }

    private void initAnnotationParameters(Query queryAnnotation) {
        this.queryTimeout = queryAnnotation.timeout();
        this.timeoutUnit = queryAnnotation.timeUnit();
        this.muonUrl = queryAnnotation.value();
    }
}
