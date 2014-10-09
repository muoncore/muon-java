package org.muoncore;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MuonResourceEvent {
    private URI uri;
    private Map<String, String> headers = new HashMap<String, String>();
    private Object payload;
    private String mimeType;

    public MuonResourceEvent(URI uri, String mimeType, Object payload) {
        this.uri = uri;
        this.payload = payload;
        this.mimeType = mimeType;
    }

    public MuonResourceEvent(Object payload) {
        this.payload = payload;
    }

    public URI getUri() { return this.uri; }

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
