package io.muoncore.spring.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventSourceListener {
    String stream() default "general";
//    Class value();
//    EventReplayMode mode() default EventReplayMode.REPLAY_THEN_LIVE;
}
