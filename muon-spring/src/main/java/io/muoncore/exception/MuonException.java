package io.muoncore.exception;

public class MuonException extends RuntimeException {
    public MuonException() {
        super();
    }

    public MuonException(String message) {
        super(message);
    }

    public MuonException(String message, Throwable cause) {
        super(message, cause);
    }

    public MuonException(Throwable cause) {
        super(cause);
    }

    protected MuonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
