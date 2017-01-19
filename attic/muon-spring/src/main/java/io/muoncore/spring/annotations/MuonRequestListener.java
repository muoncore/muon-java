package io.muoncore.spring.annotations;

import io.muoncore.spring.annotations.parameterhandlers.DecodedContent;
import io.muoncore.spring.annotations.parameterhandlers.Parameter;

import java.lang.annotation.*;

/**
 * Adds a request listener for Muon Resource queries
 * @see DecodedContent
 * @see Parameter
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MuonRequestListener {
    /**
     * Query path, should start with /.
     * For example, /resource will map all queries to muon://[serviceName]/resource
     * @return the path to listen on
     */
    String path();
}
