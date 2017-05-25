package io.muoncore.protocol.rpc.server;

public class ServerResponse {

    private int status;
    private Object payload;

    public ServerResponse(
            int status,
            Object payload) {
        this.payload = payload;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public Object getPayload() {
        return payload;
    }
}
