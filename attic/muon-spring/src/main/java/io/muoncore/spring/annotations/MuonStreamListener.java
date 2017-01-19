package io.muoncore.spring.annotations;

import java.lang.annotation.*;

/**
 * Adds a stream listener for Muon Stream subscriptions
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MuonStreamListener {
    /**
     * @return Full muon URL to subscribe to, e.g. muon://service/stream
     */
    String url();
}
