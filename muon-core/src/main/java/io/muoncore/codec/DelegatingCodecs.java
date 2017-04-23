package io.muoncore.codec;

import io.muoncore.exception.MuonEncodingException;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    log.trace("Encoding {}/{} with content type {}", object, object.getClass(), acceptableContentTypes);
    for (int i = acceptableContentTypes.length - 1; i >= 0; i--) {
      MuonCodec specificCodec = codecLookup.get(acceptableContentTypes[i]);
      if(specificCodec != null && specificCodec.canEncode(object.getClass())) {
        try {
          return new EncodingResult(specificCodec.encode(object), specificCodec.getContentType());
        } catch (UnsupportedEncodingException e) {
          log.error("Error encoding " + object + " using codec " + specificCodec, e);
        }
      }
    }
    for (int i = acceptableContentTypes.length - 1; i >= 0; i--) {
      Codecs delegateCodecs = codecsLookup.get(acceptableContentTypes[i]);
      if(delegateCodecs != null) {
        return delegateCodecs.encode(object, acceptableContentTypes);
      }
    }

    return new EncodingResult(new MuonEncodingException("Unable to encode object of type " + object.getClass() + ", no codec can handle " + Arrays.asList(acceptableContentTypes)));
  }

  @Override
  public <T> T decode(byte[] source, String contentType, Type type) throws DecodingFailureException {
    log.trace("Decoding {} with content type {} {}", type, contentType, source);
    return getCodec(contentType, muonCodec -> muonCodec.decode(source, type), codecs -> codecs.decode(source, contentType, type));
  }

  private <T> T getCodec(String contentType, Function<MuonCodec, T> execWithCodec, Function<Codecs, T> codecs) {
    MuonCodec specificCodec = codecLookup.get(contentType);

    if (specificCodec != null) {
      return execWithCodec.apply(specificCodec);
    }

    Codecs delegateCodecs = codecsLookup.get(contentType);

    if (delegateCodecs != null) {
      return codecs.apply(delegateCodecs);
    }
    log.error("Unable to decode content type {}, this is a serious misconfiguration and data is being lost", contentType);
    return null;
  }

  @Override
  public String[] getAvailableCodecs() {
    Set<String> codecs = codecLookup.values().stream().map(MuonCodec::getContentType).collect(Collectors.toSet());

    codecs.addAll(codecsLookup.values().stream().map((Codecs::getAvailableCodecs)).flatMap(Arrays::stream).collect(Collectors.toSet()));

    return codecs.toArray(new String[codecs.size()]);
  }

  @Override
  public Optional<SchemaInfo> getSchemaFor(Class type) {
    Optional<MuonCodec> codec = codecLookup.values().stream().filter(muonCodec -> muonCodec.hasSchemasFor(type)).findAny();

    log.trace
      ("Getting schema for {}, found {}", type, codec);
    return codec.map(muonCodec -> muonCodec.getSchemaInfoFor(type));
  }
}
