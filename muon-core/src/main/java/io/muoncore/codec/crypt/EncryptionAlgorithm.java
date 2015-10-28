package io.muoncore.codec.crypt;

public interface EncryptionAlgorithm {
    byte[] decrypt(byte[] input);
    byte[] encrypt(byte[] input);
    String getAlgorithmName();
}
