package io.muoncore.protocol.event;

/**
 * A canonical Event for Muon
 */
public class Event<X> {

    //precedence
    private String id;
    private String parentId;
    private String serviceId;

    private Object payload;

    public Event(String id, String parentId, String serviceId, Object payload) {
        this.id = id;
        this.parentId = parentId;
        this.serviceId = serviceId;
        this.payload = payload;
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

    public Object getPayload() {
        return payload;
    }
}
