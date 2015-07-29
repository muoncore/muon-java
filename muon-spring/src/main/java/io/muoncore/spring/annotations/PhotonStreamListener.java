package io.muoncore.spring.annotations;

import java.lang.annotation.*;

/**
 * Adds a stream listener for Photon event store stream subscriptions
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PhotonStreamListener {

    String photonUrl() default "muon://photon/stream";

    String streamName();

    int from() default 0;

    StreamType streamType() default StreamType.HOT;
}
