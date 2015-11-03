package io.muoncore.codec.json;

import io.muoncore.codec.Codecs;
import io.muoncore.codec.MuonCodec;
import io.muoncore.exception.MuonException;

import java.io.UnsupportedEncodingException;

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
    public String getBestAvailableCodec(String[] acceptableContentTypes) {
        return defaultCodec.getContentType();
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
    public <T> T decode(byte[] source, String contentType, Class<T> type) {
        if (source == null) return null;
        return defaultCodec.decode(source, type);
    }
}
