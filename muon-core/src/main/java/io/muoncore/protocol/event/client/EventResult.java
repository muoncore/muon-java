package io.muoncore.protocol.event.client;

public class EventResult {

    private EventResultStatus status;
    private String cause;


    public EventResult(EventResultStatus status, String cause) {
        this.status = status;
        this.cause = cause;
    }

    public EventResultStatus getStatus() {
        return status;
    }

    public String getCause() {
        return cause;
    }

    public enum EventResultStatus {
        PERSISTED, FAILED
    }
}

