package io.muoncore.codec;

import io.muoncore.exception.MuonEncodingException;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class DelegatingCodecs implements Codecs {
  private Map<String, MuonCodec> codecLookup = new HashMap<>();
  private Map<String, Codecs> codecsLookup = new HashMap<>();

  public DelegatingCodecs withCodecs(Codecs codecs) {
    for (String content : codecs.getAvailableCodecs()) {
      codecsLookup.put(content, codecs);
    }
    return this;
  }

  public DelegatingCodecs withCodec(MuonCodec codecs) {
    codecLookup.put(codecs.getContentType(), codecs);
    return this;
  }

  @Override
  public <T> EncodingResult encode(T object, String[] acceptableContentTypes) {
    log.debug("Encoding {} with content type {}", object, acceptableContentTypes);
    for (int i = acceptableContentTypes.length - 1; i >= 0; i--) {
      return getCodec(acceptableContentTypes[i], muonCodec -> {
        try {
          return new EncodingResult(muonCodec.encode(object), muonCodec.getContentType());
        } catch (UnsupportedEncodingException e) {
          log.error("Error encoding " + object, e);
          return null;
        }
      }, codecs -> codecs.encode(object, acceptableContentTypes));
    }

    return new EncodingResult(new MuonEncodingException("Unable to encode object of type " + object.getClass() + ", no codec can handle " + Arrays.asList(acceptableContentTypes)));
  }

  @Override
  public <T> T decode(byte[] source, String contentType, Type type) throws DecodingFailureException {
    log.debug("Decoding {} with content type {}", type, contentType);
    return getCodec(contentType, muonCodec -> muonCodec.decode(source, type), codecs -> codecs.decode(source, contentType, type));
  }

  private <T> T getCodec(String contentType, Function<MuonCodec, T> codec, Function<Codecs, T> codecs) {
    MuonCodec exec = codecLookup.get(contentType);

    if (exec != null) {
      return codec.apply(exec);
    }

    Codecs exec2 = codecsLookup.get(contentType);

    if (exec2 != null) {
      return codecs.apply(exec2);
    }
    log.error("Unable to decode content type {}, this is a serious misconfiguration and data is being lost", contentType);
    return null;
  }

  @Override
  public String[] getAvailableCodecs() {
    return codecLookup.values().stream().map(MuonCodec::getContentType).toArray(String[]::new);
  }
}
