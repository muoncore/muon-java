package io.muoncore.spring.annotations;

import java.lang.annotation.*;

/**
 * Adds a query listener for Muon Resource commands
 * @see io.muoncore.spring.annotations.parameterhandlers.DecodedContent
 * @see io.muoncore.spring.annotations.parameterhandlers.MuonHeaders
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MuonCommandListener {
    /**
     * Query path, should start with /.
     * For example, /resource will map all commands to muon://[serviceName]/resource
     * @return the path to listen on
     */
    String path();
}
