package io.muoncore.codec;

import java.util.Map;

public interface TextCodec {
    public <T> T decode(String encodedData, Class<T> type);
    public Map decode(String encodedData);
    public String encode(Object data);
    String getContentType();
}
