package io.muoncore.exception;


import lombok.Getter;

public class MuonProtocolException extends MuonException {

  @Getter
  private String protocol;

  public MuonProtocolException(String protocol, String message, Throwable cause) {
    super(String.format("Protocol %s: %s", protocol, message), cause);
    this.protocol = protocol;
  }
}
