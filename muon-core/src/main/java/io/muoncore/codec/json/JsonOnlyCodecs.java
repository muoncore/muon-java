package io.muoncore.codec.json;

import io.muoncore.codec.Codecs;
import io.muoncore.codec.MuonCodec;
import io.muoncore.exception.MuonEncodingException;
import io.muoncore.exception.MuonException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class JsonOnlyCodecs implements Codecs {

    private MuonCodec defaultCodec;

    public JsonOnlyCodecs() {
        defaultCodec = new GsonCodec();
    }

    @Override
    public String[] getAvailableCodecs() {
        return new String[] { defaultCodec.getContentType() };
    }

    @Override
    public EncodingResult encode(Object object, String[] acceptableContentTypes) {
        try {
            return new EncodingResult(defaultCodec.encode(object), defaultCodec.getContentType());
        } catch (UnsupportedEncodingException e) {
            return new EncodingResult(new MuonException("Unable to encode object " + object.getClass().getSimpleName(), e));
        }
    }

    @Override
    public <T> T decode(byte[] source, String contentType, Type type) {
        if (!contentType.equals(defaultCodec.getContentType())) {
            throw new MuonEncodingException("Content type " + contentType + " is not supported. This codec supports " + Arrays.toString(getAvailableCodecs()));
        }
        if (source == null) return null;
        return defaultCodec.decode(source, type);
    }

  @Override
  public Optional<SchemaInfo> getSchemaFor(Class type) {
    return Optional.empty();
  }
}
