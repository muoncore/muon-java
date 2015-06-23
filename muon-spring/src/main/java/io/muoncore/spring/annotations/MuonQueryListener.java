package io.muoncore.spring.annotations;

import java.lang.annotation.*;

/**
 * Adds a query listener for Muon Resource queries
 * @see io.muoncore.spring.annotations.parameterhandlers.DecodedContent
 * @see io.muoncore.spring.annotations.parameterhandlers.MuonHeaders
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MuonQueryListener {
    /**
     * Query path, should start with /.
     * For example, /resource will map all queries to muon://<serviceName>/resource
     */
    String path();
}
