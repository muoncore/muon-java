package io.muoncore.codec;

import java.io.UnsupportedEncodingException;

public class JsonOnlyCodecs implements Codecs {

    private MuonCodec defaultCodec;

    public JsonOnlyCodecs() {
        defaultCodec = new GsonCodec();
    }

    @Override
    public String getBestAvailableCodec(String[] acceptableContentTypes) {
        return defaultCodec.getContentType();
    }

    @Override
    public EncodingResult encode(Object object, String[] acceptableContentTypes) throws UnsupportedEncodingException {
        return new EncodingResult(defaultCodec.encode(object), defaultCodec.getContentType());
    }

    @Override
    public <T> T decode(byte[] source, String contentType, Class<T> type) {
        if (source == null) return null;
        return defaultCodec.decode(source, type);
    }
}