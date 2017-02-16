package io.muoncore.protocol.reactivestream.client;

import io.muoncore.codec.Codecs;
import io.muoncore.message.MuonInboundMessage;

import java.lang.reflect.Type;

public class StreamData {

  private MuonInboundMessage source;
  private Codecs codecs;

  public StreamData(MuonInboundMessage source, Codecs codecs) {
    this.source = source;
    this.codecs = codecs;
  }

  public <T> T getPayload(Class<T> type) {
    return codecs.decode(source.getPayload(), source.getContentType(), type);
  }

  public <T> T getPayload(Type type) {
    return codecs.decode(source.getPayload(), source.getContentType(), type);
  }
}
