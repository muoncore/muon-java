package io.muoncore.spring.annotations;

import io.muoncore.protocol.event.client.EventReplayMode;

public @interface EventSourceListener {
    String name() default "general";
    Class value();
    EventReplayMode mode() default EventReplayMode.REPLAY_THEN_LIVE;
}
