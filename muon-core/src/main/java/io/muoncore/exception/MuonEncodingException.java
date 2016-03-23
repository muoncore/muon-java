package io.muoncore.exception;

public class MuonEncodingException extends MuonException {
    public MuonEncodingException() {
        super();
    }

    public MuonEncodingException(String message) {
        super(message);
    }

    public MuonEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MuonEncodingException(Throwable cause) {
        super(cause);
    }

    protected MuonEncodingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
