package io.muoncore.protocol.event;

import com.google.gson.annotations.SerializedName;

/**
 * A canonical Event for Muon
 */
public class Event<X> {

    @SerializedName("event-type")
    private String eventType;
    @SerializedName("stream-name")
    private String streamName;

    private String schema;
    @SerializedName("caused-by-id")
    private Long causedById;
    @SerializedName("caused-by-relation")
    private String causedByRelation;

    @SerializedName("service-id")
    private String service;
    @SerializedName("order-id")
    private Long orderId;
    @SerializedName("server-timestamp")
    private Long eventTime;
    private X payload;

    public Event(String eventType, String streamName, String schema, Long causedById, String causedByRelation, String service, Long orderId, Long eventTime, X payload) {
        this.eventType = eventType;
        this.streamName = streamName;
        this.schema = schema;
        this.causedById = causedById;
        this.causedByRelation = causedByRelation;
        this.service = service;
        this.orderId = orderId;
        this.eventTime = eventTime;
        this.payload = payload;
    }

    public String getEventType() {
        return eventType;
    }

    public String getStreamName() {
        return streamName;
    }

    public String getSchema() {
        return schema;
    }

    public Long getCausedById() {
        return causedById;
    }

    public String getCausedByRelation() {
        return causedByRelation;
    }

    public String getService() {
        return service;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getEventTime() {
        return eventTime;
    }

    public X getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventType='" + eventType + '\'' +
                ", streamName='" + streamName + '\'' +
                ", schema='" + schema + '\'' +
                ", causedById=" + causedById +
                ", causedByRelation='" + causedByRelation + '\'' +
                ", service='" + service + '\'' +
                ", orderId=" + orderId +
                ", eventTime=" + eventTime +
                '}';
    }
}
