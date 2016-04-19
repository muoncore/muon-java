package io.muoncore.protocol.requestresponse;

import java.net.URI;

public class Request {

    private URI url;
    private Object payload;

    public Request(
            URI url,
            Object payload) {
        this.url = url;
        this.payload = payload;
    }

    public URI getUrl() {
        return url;
    }

    public Object getPayload() {
        return payload;
    }
}
