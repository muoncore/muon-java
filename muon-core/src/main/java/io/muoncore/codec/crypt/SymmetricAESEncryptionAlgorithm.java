package io.muoncore.codec.crypt;

import io.muoncore.exception.MuonException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class SymmetricAESEncryptionAlgorithm implements EncryptionAlgorithm {

    private final String encodingkey;
    final static String IV = "AAAAAAAAAAAAAAAA";
    final private SecretKeySpec key;

    public SymmetricAESEncryptionAlgorithm(String key) {
        if (key == null) {
            throw new MuonException("AES Key is not set, unable to initialise encryption algorithm");
        }
        this.encodingkey = key;
        try {
            this.key = new SecretKeySpec(encodingkey.getBytes("UTF-8"), "AES");
        } catch (UnsupportedEncodingException e) {
            throw new MuonException("AES Key is invalid", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
            cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
            return cipher.doFinal(input);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | BadPaddingException | UnsupportedEncodingException | InvalidKeyException e) {
            throw new MuonException("Unable to decrypt payload", e);
        }
    }

    @Override
    public byte[] encrypt(byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
            SecretKeySpec key = new SecretKeySpec(encodingkey.getBytes("UTF-8"), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));
            return cipher.doFinal(input);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException | InvalidKeyException | UnsupportedEncodingException e) {
            throw new MuonException("Unable to encrypt payload", e);
        }
    }

    @Override
    public String getAlgorithmName() {
        return "AES";
    }
}
