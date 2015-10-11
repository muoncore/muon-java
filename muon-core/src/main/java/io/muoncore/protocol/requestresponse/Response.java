package io.muoncore.protocol.requestresponse;

public class Response<X> {

    private String url;

    public Response(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
