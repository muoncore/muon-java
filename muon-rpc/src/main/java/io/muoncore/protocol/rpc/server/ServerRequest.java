package io.muoncore.protocol.rpc.server;

import com.google.gson.annotations.SerializedName;
import io.muoncore.codec.Codecs;
import io.muoncore.protocol.Auth;

import java.lang.reflect.Type;
import java.net.URI;

public class ServerRequest {

  private URI url;
  @SerializedName("body")
  private byte[] payload;
  @SerializedName("content_type")
  private String contentType;
  private transient Codecs codecs;

  private Auth auth;

  public ServerRequest(
    URI url,
    Auth auth,
    byte[] payload,
    String contentType,
    Codecs codecs) {
    this.url = url;
    this.auth = auth;
    this.payload = payload;
    this.codecs = codecs;
    this.contentType = contentType;
  }

  public Auth getAuth() {
    return auth;
  }

  public void setCodecs(Codecs codecs) {
    this.codecs = codecs;
  }

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
