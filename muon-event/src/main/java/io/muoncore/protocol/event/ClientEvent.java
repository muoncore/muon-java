package io.muoncore.protocol.event;

/**
 * An event created by a client, ready to be passed to an event store for persistence and cononicalisation
 */
public class ClientEvent<X> {


    private String eventType;
    private String streamName;
    private X payload;

    private String causedById;
    private String causedByRelation;
    private String schema;

    public ClientEvent(
            String eventType,
            String streamName,
            String schema,
            String causedById,
            String causedByRelation,
            X payload) {
        this.schema = schema;
        this.eventType = eventType;
        this.streamName = streamName;
        this.payload = payload;
        this.causedById = causedById;
        this.causedByRelation = causedByRelation;
    }

    public String getSchema() {
        return schema;
    }

    public String getEventType() {
        return eventType;
    }

    public String getStreamName() {
        return streamName;
    }

    public X getPayload() {
        return payload;
    }

    public String getCausedById() {
        return causedById;
    }

    public String getCausedByRelation() {
        return causedByRelation;
    }

    @Override
    public String toString() {
        return "ClientEvent{" +
                "eventType='" + eventType + '\'' +
                ", streamName='" + streamName + '\'' +
                ", causedById='" + causedById + '\'' +
                ", causedByRelation='" + causedByRelation + '\'' +
                ", schema='" + schema + '\'' +
                '}';
    }
}
