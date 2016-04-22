package io.muoncore.protocol.requestresponse.server;

import com.google.gson.annotations.SerializedName;
import io.muoncore.codec.Codecs;

import java.lang.reflect.Type;
import java.net.URI;

public class ServerRequest {

    private URI url;
    @SerializedName("body")
    private byte[] payload;
    @SerializedName("content_type")
    private String contentType;
    private transient Codecs codecs;

    public ServerRequest(
            URI url,
            byte[] payload,
            String contentType,
            Codecs codecs) {
        this.url = url;
        this.payload = payload;
        this.codecs = codecs;
        this.contentType = contentType;
    }

    public void setCodecs(Codecs codecs) { this.codecs = codecs; }
    public URI getUrl() {
        return url;
    }

    public <X> X getPayload(Class<X> type) {
        return codecs.decode(payload, contentType, type);
    }
    public <X> X getPayload(Type type) {
        return codecs.decode(payload, contentType, type);
    }
}
