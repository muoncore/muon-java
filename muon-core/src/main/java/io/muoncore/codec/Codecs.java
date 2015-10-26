package io.muoncore.codec;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface Codecs {
    EncodingResult encode(Object object, String[] acceptableContentTypes) throws UnsupportedEncodingException;
    <T> T decode(byte[] source, String contentType, Class<T> type);

    String getBestAvailableCodec(String[] acceptableContentTypes);

    List<String> getAvailableCodecs();

    class EncodingResult {
        private byte[] payload;
        private String contentType;

        public EncodingResult(byte[] payload, String contentType) {
            this.payload = payload;
            this.contentType = contentType;
        }

        public byte[] getPayload() {
            return payload;
        }

        public String getContentType() {
            return contentType;
        }
    }
}
