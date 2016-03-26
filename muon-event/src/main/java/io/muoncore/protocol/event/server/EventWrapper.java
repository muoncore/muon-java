package io.muoncore.protocol.event.server;

import io.muoncore.protocol.event.Event;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.protocol.event.client.EventResult;
import io.muoncore.protocol.event.Event;

public class EventWrapper {

    private Event event;
    private ChannelConnection<EventResult, ?> channel;

    public EventWrapper(Event event, ChannelConnection<EventResult, ?> channel) {
        this.event = event;
        this.channel = channel;
    }

    public Event getEvent() {
        return event;
    }

    public void persisted() {
        channel.send(new EventResult(
                EventResult.EventResultStatus.PERSISTED, "Event persisted"
        ));
    }

    public void failed(String reason) {
        channel.send(new EventResult(
                EventResult.EventResultStatus.FAILED, reason
        ));
    }
}