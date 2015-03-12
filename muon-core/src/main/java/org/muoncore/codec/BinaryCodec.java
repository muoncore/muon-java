package org.muoncore.codec;

import java.util.Map;

public interface BinaryCodec {
    public <T> T decode(byte[] encodedData, Class<T> type);
    public Map decode(byte[] encodedData);
    public byte[] encode(Object data);
    String getContentType();
}
