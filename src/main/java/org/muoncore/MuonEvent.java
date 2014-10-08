package org.muoncore;

import java.util.HashMap;
import java.util.Map;

public class MuonEvent {
    private String serviceId;
    private String resource;
    private Map<String, String> headers = new HashMap<String, String>();
    private Object payload;

    public MuonEvent(String serviceId, String resource, Object payload) {
        this.serviceId = serviceId;
        this.resource = resource;
        this.payload = payload;
    }

    public MuonEvent(String resource, Object payload) {
        this.resource = resource;
        this.payload = payload;
    }

    public void addHeader(String header, String value) {
        headers.put(header, value);
    }

    public String getResource() {
        return resource;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Object getPayload() {
        return payload;
    }

    public String getServiceId() {
        return serviceId;
    }
}
