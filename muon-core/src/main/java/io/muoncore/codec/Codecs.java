package io.muoncore.codec;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Type;
import java.util.Optional;

public interface Codecs {
  <T> EncodingResult encode(T object, String[] acceptableContentTypes);

  <T> T decode(byte[] source, String contentType, Type type) throws DecodingFailureException;

  String[] getAvailableCodecs();

  Optional<SchemaInfo> getSchemaFor(Class type);

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

  @Data
  @AllArgsConstructor
  class SchemaInfo {
    private String schemaText;
    private String schemaType;
  }
}
