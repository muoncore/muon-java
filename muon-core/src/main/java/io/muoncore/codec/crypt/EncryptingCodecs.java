package io.muoncore.codec.crypt;

import io.muoncore.codec.Codecs;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Codecs that wraps another and encrypts/ decrypts on the way past.
 */
public class EncryptingCodecs implements Codecs {

    private Codecs delegate;
    private EncryptionAlgorithm algorithm;

    public EncryptingCodecs(Codecs delegate, EncryptionAlgorithm algorithm) {
        this.delegate = delegate;
        this.algorithm = algorithm;
    }

    @Override
    public EncodingResult encode(Object object, String[] acceptableContentTypes) throws UnsupportedEncodingException {
        EncodingResult result = delegate.encode(object, acceptableContentTypes);
        return new EncodingResult(algorithm.encrypt(result.getPayload()),
                result.getContentType() + "+" + algorithm.getAlgorithmName());
    }

    @Override
    public <T> T decode(byte[] source, String contentType, Class<T> type) {
        byte[] decrypted = algorithm.decrypt(source);
        String content = contentType.substring(0, contentType.indexOf("+"));
        return delegate.decode(decrypted, content, type);
    }

    @Override
    public String getBestAvailableCodec(String[] acceptableContentTypes) {
        return delegate.getBestAvailableCodec(acceptableContentTypes) + "+" + algorithm.getAlgorithmName();
    }

    @Override
    public String[] getAvailableCodecs() {
        return Arrays.stream(delegate.getAvailableCodecs()).map(
                codec -> codec + "+" + algorithm.getAlgorithmName()).collect(Collectors.toList()).toArray(new String[0]);
    }
}
