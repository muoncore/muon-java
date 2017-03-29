package io.muoncore.codec;

import io.muoncore.exception.MuonEncodingException;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    try {
      for (int i = acceptableContentTypes.length - 1; i >= 0; i--) {
        MuonCodec codec = codecLookup.get(acceptableContentTypes[i]);
        if (codec != null) {
          log.trace(" Encoding {} with contents {} and codec {}", object, acceptableContentTypes, codec);
          return new EncodingResult(codec.encode(object), codec.getContentType());
        }
      }

      return new EncodingResult(new MuonEncodingException("Unable to encode object of type " + object.getClass() + ", no codec can handle " + Arrays.asList(acceptableContentTypes)));
    } catch (UnsupportedEncodingException e) {
      return new EncodingResult(e);
    }
  }

  @Override
  public <T> T decode(byte[] source, String contentType, Type type) throws DecodingFailureException {
    return codecLookup.get(contentType).decode(source, type);
  }

  @Override
  public String[] getAvailableCodecs() {
    return codecLookup.values().stream().map(MuonCodec::getContentType).toArray(String[]::new);
  }
}
