package io.muoncore.protocol.requestresponse;

public class Request<X> {

    private String id;
    private String url;
    private X payload;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public X getPayload() {
        return payload;
    }
}
