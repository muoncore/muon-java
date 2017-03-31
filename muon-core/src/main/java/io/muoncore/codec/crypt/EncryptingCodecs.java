package io.muoncore.codec.crypt;

import io.muoncore.codec.Codecs;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

        boolean encryptedContentType = false;
        for(String contentType: acceptableContentTypes) {
            if (contentType.indexOf("+") > 0) {
                encryptedContentType = true;
            }
        }

        if(encryptedContentType) {
            return new EncodingResult(algorithm.encrypt(result.getPayload()),
                    result.getContentType() + "+" + algorithm.getAlgorithmName());
        } else {
            return result;
        }
    }

    @Override
    public <T> T decode(byte[] source, String contentType, Type type) {
        boolean encryptedContentType = contentType.indexOf("+") > 0;
        byte[] decrypted;
        String decryptedContentType;
        if (encryptedContentType) {
            decrypted = algorithm.decrypt(source);
            decryptedContentType = contentType.substring(0, contentType.indexOf("+"));
        } else {
            decrypted = source;
            decryptedContentType = contentType;
        }

        return delegate.decode(decrypted, decryptedContentType, type);
    }

    @Override
    public String[] getAvailableCodecs() {
        List<String> codecs = new ArrayList<>();
        codecs.addAll(Arrays.asList(delegate.getAvailableCodecs()));

        codecs.addAll(Arrays.stream(delegate.getAvailableCodecs()).map(
                codec -> codec + "+" + algorithm.getAlgorithmName()).collect(Collectors.toList()));

        return codecs.toArray(new String[codecs.size()]);
    }

  @Override
  public Optional<SchemaInfo> getSchemaFor(Class type) {
    return Optional.empty();
  }
}
