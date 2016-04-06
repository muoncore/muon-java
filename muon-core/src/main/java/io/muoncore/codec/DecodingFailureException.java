package io.muoncore.codec;

public class DecodingFailureException extends RuntimeException {
    public DecodingFailureException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
