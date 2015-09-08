package io.muoncore.spring.annotations;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Performs a Command to a remote service.
 * @see io.muoncore.spring.annotations.MuonRepository
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Command {
    /**
     * Command full url, should start with muon://.
     * For example, muon://service/resource
     * @return the path to send commands to
     */
    String value();

    /**
     * @return timeout to wait for response
     */
    int timeout() default 15;

    /**
     * @return Wait timeout units
     * @see io.muoncore.spring.annotations.Command#timeout
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

}
