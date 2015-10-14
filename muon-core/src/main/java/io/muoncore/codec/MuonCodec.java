package io.muoncore.codec;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface MuonCodec {
    public <T> T decode(byte[] encodedData, Class<T> type);
    public Map decode(byte[] encodedData);
    public byte[] encode(Object data) throws UnsupportedEncodingException;
    String getContentType();
}
