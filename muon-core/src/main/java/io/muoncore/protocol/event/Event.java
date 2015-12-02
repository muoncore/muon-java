package io.muoncore.protocol.event;

/**
 * A canonical Event for Muon
 */
public class Event<X> {

    public final static String START="START";

    //precedence
    private String id;
    private String parentId;
    private String serviceId;

    private String eventType;

    private X payload;

    public Event(String type, String id, String parentId, String serviceId, X payload) {
        this.id = id;
        this.parentId = parentId;
        this.serviceId = serviceId;
        this.payload = payload;
    }

    public String getEventType() {
        return eventType;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public X getPayload() {
        return payload;
    }
}
