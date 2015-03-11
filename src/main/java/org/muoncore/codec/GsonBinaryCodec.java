package org.muoncore.codec;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class GsonBinaryCodec implements BinaryCodec {

    private GsonTextCodec delegate = new GsonTextCodec();

    @Override
    public <T> T decode(byte[] encodedData, Class<T> type) {
        try {
            return delegate.decode(new String(encodedData, "UTF8"), type);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unable to read byte array", e);
        }
    }

    @Override
    public Map decode(byte[] encodedData) {
        try {
            return delegate.decode(new String(encodedData, "UTF8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unable to read byte array", e);
        }
    }

    @Override
    public byte[] encode(Object data) {
        try {
            return delegate.encode(data).getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unable to convert to byte array", e);
        }
    }
}
