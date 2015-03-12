package org.muoncore.codec;

import java.util.Base64;
import java.util.Map;

/**
 * Given a BinaryCodec as a delegate, will manage the translation to text
 * as needed. This allows any binary codec to be transferred across a transport
 * that is naturally text based, such as HTTP.
 */
public class Base64TextCodec implements TextCodec {

    private BinaryCodec delegate;
    private Base64.Encoder encoder;
    private Base64.Decoder decoder;

    public Base64TextCodec(BinaryCodec delegate) {
        this.delegate = delegate;
        encoder = Base64.getEncoder();
        decoder = Base64.getDecoder();
    }

    @Override
    public <T> T decode(String encodedData, Class<T> type) {
        byte[] value = decoder.decode(encodedData);
        return delegate.decode(value, type);
    }

    @Override
    public Map decode(String encodedData) {
        byte[] value = decoder.decode(encodedData);
        return delegate.decode(value);
    }

    @Override
    public String encode(Object data) {
        byte[] val = delegate.encode(data);
        return encoder.encodeToString(val);
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }
}
