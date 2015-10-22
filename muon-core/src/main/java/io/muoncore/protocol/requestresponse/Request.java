package io.muoncore.protocol.requestresponse;

import java.util.UUID;

public class Request<X> {

    public final static String URL = "url";

    private String id;
    private RequestMetaData metaData;
    private X payload;

    public Request(
            RequestMetaData metaData,
            X payload) {
        this.id = UUID.randomUUID().toString();
        this.metaData = metaData;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RequestMetaData getMetaData() {
        return metaData;
    }

    public X getPayload() {
        return payload;
    }
}
