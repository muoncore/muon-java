package org.muoncore;


import java.util.HashMap;
import java.util.Map;

public class MuonBroadcastEvent {
    private String eventName;
    private String mimeType;
    private Map<String, String> headers = new HashMap<String, String>();
    private Object payload;

    public MuonBroadcastEvent(String eventName, String mimeType, Object payload) {
        this.eventName = eventName;
        this.mimeType = mimeType;
        this.payload = payload;
    }

    public MuonBroadcastEvent(Object payload) {
        this.payload = payload;
    }

    public void addHeader(String header, String value) {
        headers.put(header, value);
    }

    public String getEventName() {
        return eventName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Object getPayload() {
        return payload;
    }

}
