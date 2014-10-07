package org.muoncore;

import java.util.HashMap;
import java.util.Map;

public class MuonEvent {
    private String resource;
    private Map<String, String> headers = new HashMap<String, String>();
    private Object payload;

    public MuonEvent(String resource, Object payload) {
        this.resource = resource;
        this.payload = payload;
    }

    public void addHeader(String header, String value) {
        headers.put(header, value);
    }

}
