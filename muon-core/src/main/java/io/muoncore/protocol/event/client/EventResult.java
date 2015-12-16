package io.muoncore.protocol.event.client;

public class EventResult {

    private EventResultStatus status;


    public EventResult(EventResultStatus status) {
        this.status = status;
    }

    public EventResultStatus getStatus() {
        return status;
    }

    public static enum EventResultStatus {
        PERSISTED, FAILED
    }
}

