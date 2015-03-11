package org.muoncore.codec;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;

/**
 * Given a TextCodec as a delegate, this will manage the conversion of a byte array
 * into/ from text. This allows any text based codec to be used where the transport
 * is naturally binary, such as AMQP.
 */
public class TextBinaryCodec implements BinaryCodec {

    private TextCodec delegate;

    public TextBinaryCodec(TextCodec delegate) {
        this.delegate = delegate;
    }

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
