package io.muoncore.protocol.event.client;

public class EventResult {

    private long eventTime;
    private String orderId;
    private EventResultStatus status;
    private String cause;

    public EventResult(EventResultStatus status, String cause) {
        this.status = status;
        this.cause = cause;
    }

    public EventResult(EventResultStatus status, String cause, String orderId, long eventTime) {
        this.status = status;
        this.cause = cause;
        this.orderId = orderId;
        this.eventTime = eventTime;
    }

    public long getEventTime() {
        return eventTime;
    }

    public String getOrderId() {
        return orderId;
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

