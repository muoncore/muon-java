package io.muoncore.protocol.requestresponse;

public class Response<X> {

    private String id;
    private String url;

    public Response(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }
}
