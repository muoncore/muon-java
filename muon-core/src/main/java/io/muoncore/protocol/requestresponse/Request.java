package io.muoncore.protocol.requestresponse;

public class Request<X> {

    private String id;
    private X payload;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
