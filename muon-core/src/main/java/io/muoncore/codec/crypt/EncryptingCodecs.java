package io.muoncore.codec.crypt;

import io.muoncore.codec.Codecs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public EncodingResult encode(Object object, String[] acceptableContentTypes) {
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
    public String[] getAvailableCodecs() {
        List<String> codecs = new ArrayList<>();
        codecs.addAll(Arrays.asList(delegate.getAvailableCodecs()));

        codecs.addAll(Arrays.stream(delegate.getAvailableCodecs()).map(
                codec -> codec + "+" + algorithm.getAlgorithmName()).collect(Collectors.toList()));

        return codecs.toArray(new String[codecs.size()]);
    }
}
