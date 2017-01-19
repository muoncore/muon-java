package io.muoncore.spring.annotations;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Performs a Request to a remote service.
 * @see MuonRepository
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Request {
    /**
     * Command full url, should start with request://.
     * For example, request://service/resource
     * @return the path to send commands to
     */
    String value();

    /**
     * @return timeout to wait for response
     */
    int timeout() default 15;

    /**
     * @return Wait timeout units
     * @see Request#timeout
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

}
