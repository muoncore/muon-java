package io.muoncore.codec;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

public interface MuonCodec {

    <T> T decode(byte[] encodedData, Type type);

    byte[] encode(Object data) throws UnsupportedEncodingException;

    String getContentType();

    boolean canEncode(Class type);
    boolean hasSchemasFor(Class type);
    Codecs.SchemaInfo getSchemaInfoFor(Class type);
}
