package io.muoncore.codec.avro;

import io.muoncore.codec.Codecs;

public interface AvroConverter {
  <T> T decode(byte[] encodedData);

  byte[] encode(Object data);

  Codecs.SchemaInfo getSchemaInfoFor(Class type);

  boolean hasSchemasFor(Class type);
}
