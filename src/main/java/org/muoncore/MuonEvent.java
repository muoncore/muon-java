package org.muoncore;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MuonEvent {
    private URI uri;
    private Map<String, String> headers = new HashMap<String, String>();
    private Object payload;

    public MuonEvent(URI uri, String mimeType, Object payload) {
        this.payload = payload;
    }

    public MuonEvent(Object payload) {
        this.payload = payload;
    }

    public void addHeader(String header, String value) {
        headers.put(header, value);
    }

    public String getResource() {
        return uri.getPath();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Object getPayload() {
        return payload;
    }

    public String getServiceId() {
        return uri.getHost();
    }
}
