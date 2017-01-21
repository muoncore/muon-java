package io.muoncore.protocol;

public class Auth {

  private String provider;
  private String token;

  public Auth(String provider, String token) {
    this.provider = provider;
    this.token = token;
  }

  public String getProvider() {
    return provider;
  }

  public String getToken() {
    return token;
  }

  @Override
  public String toString() {
    return "Auth{" +
      "provider='" + provider + '\'' +
      ", token='" + token + '\'' +
      '}';
  }
}
