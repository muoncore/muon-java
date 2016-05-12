package io.muoncore.protocol.event;

/**
 * Construct a ClientEvent using a fluent builder.
 * @param <X>
 */
public class EventBuilder<X> {
    private X payload;
    private String eventType;
    private String streamName = "default";

    private Long causedById;
    private String causedByRelation;
    private String schema;

    /**
     * The payload of this event. can be any object that can be serialised using an existing codec.
     *
     * Optional.
     */
    public EventBuilder<X> payload(X payload) {
        this.payload = payload;
        return this;
    }

    /**
     * Set the schema version of this event. This can be used later on during projection or replay
     * to enable auto upgrade of the event schema during replay and projection.
     *
     * Optional
     */
    public EventBuilder<X> schema(String schema) {
        this.schema = schema;
        return this;
    }

    /**
     * The event type. This gives the event an identity
     *
     * Mandatory.
     */
    public EventBuilder<X> eventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    /**
     * Which stream this event should persist on.
     *
     * if not supplied, will be set to 'default'
     */
    public EventBuilder<X> stream(String streamName) {
        this.streamName = streamName;
        return this;
    }

    /**
     * Add a causal relationship to another event.
     *
     * Optional
     */
    public EventBuilder<X> causedBy(Long causedById, String relation) {
        this.causedById = causedById;
        this.causedByRelation = relation;
        return this;
    }

    public ClientEvent build() {
        return new ClientEvent<>(
                eventType,
                streamName,
                schema,
                causedById,
                causedByRelation,
                payload);
    }
}
