package io.muoncore.protocol.requestresponse;

import java.util.UUID;

public class Request<X> {

    public final static String URL = "url";

    private String id;
    private Headers headers;
    private X payload;

    public Request(
            Headers headers,
            X payload) {
        this.id = UUID.randomUUID().toString();
        this.headers = headers;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Headers getHeaders() {
        return headers;
    }

    public X getPayload() {
        return payload;
    }
}
