package io.muoncore.protocol.rpc;

import io.muoncore.protocol.Auth;

import java.net.URI;

public class Request {

  private URI url;
  private Auth auth;
  private Object payload;

  public Request(
    URI url,
    Object payload) {
    this(url, payload, null);
  }

  public Request(
    URI url,
    Object payload,
    Auth auth) {
    this.url = url;
    this.payload = payload;
    this.auth = auth;
  }

  public Auth getAuth() {
    return auth;
  }

  public URI getUrl() {
    return url;
  }

  public Object getPayload() {
    return payload;
  }

  public String getTargetService() {
    return this.url.getHost();
  }
}
