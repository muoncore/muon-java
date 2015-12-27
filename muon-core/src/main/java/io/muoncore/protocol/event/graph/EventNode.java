package io.muoncore.protocol.event.graph;

import java.util.List;

public class EventNode {
    private String id;
    private String parentId;
    private String serviceId;
    private String eventType;

    private List<EventNode> children;

    public EventNode(String id, String parentId, String serviceId, String eventType, List<EventNode> children) {
        this.id = id;
        this.parentId = parentId;
        this.serviceId = serviceId;
        this.eventType = eventType;
        this.children = children;
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

    public String getEventType() {
        return eventType;
    }

    public List<EventNode> getChildren() {
        return children;
    }
}
