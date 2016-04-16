package io.muoncore.protocol.requestresponse;

import java.util.UUID;

public class Response<X> {

    public final static String STATUS = "status";

    private String id;
    private X payload;
    private int status;

    public Response(int status, X payload) {
        this.id = UUID.randomUUID().toString();
        this.payload = payload;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public X getPayload() {
        return payload;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Response{" +
                "id='" + id + '\'' +
                ", payload=" + payload +
                ", status=" + status +
                '}';
    }
}
