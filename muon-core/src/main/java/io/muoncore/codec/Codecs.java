package io.muoncore.codec;

import java.lang.reflect.Type;

public interface Codecs {
    EncodingResult encode(Object object, String[] acceptableContentTypes);
    <T> T decode(byte[] source, String contentType, Type type);

    String[] getAvailableCodecs();

    class EncodingResult {
        private byte[] payload;
        private String contentType;
        private Exception failureMessage;

        public EncodingResult(Exception failure) {
            this.failureMessage = failure;
        }

        public boolean isFailed() {
            return failureMessage != null;
        }

        public Exception getFailureMessage() {
            return failureMessage;
        }

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
