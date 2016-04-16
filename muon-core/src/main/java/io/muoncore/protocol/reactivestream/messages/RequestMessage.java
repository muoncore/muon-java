package io.muoncore.protocol.reactivestream.messages;

public class RequestMessage {
    private long request;

    public RequestMessage(long request) {
        this.request = request;
    }

    public long getRequest() {
        return request;
    }
}
