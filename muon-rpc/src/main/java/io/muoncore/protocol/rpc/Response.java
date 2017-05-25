package io.muoncore.protocol.rpc;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.muoncore.codec.Codecs;

import java.lang.reflect.Type;

public class Response {

    public final static String STATUS = "status";

    @SerializedName("body")
    private byte[] payload;
    @Expose(deserialize = false)
    private transient Codecs codecs;
    private int status;
    @SerializedName("content_type")
    private String contentType;

    public Response(int status, byte[] payload, String contentType, Codecs codecs) {
        this.payload = payload;
        this.status = status;
        this.codecs = codecs;
        this.contentType = contentType;
    }

    public void setCodecs(Codecs codecs) {
        this.codecs = codecs;
    }

    public int getStatus() {
        return status;
    }

    public <X> X getPayload(Class<X> type) {
        return codecs.decode(payload, contentType, type) ;
    }
    public <X> X getPayload(Type type) {
        return codecs.decode(payload, contentType, type) ;
    }

    @Override
    public String toString() {
        return "Response{" +
                "status=" + status +
                '}';
    }
}
