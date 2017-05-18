package io.muoncore.exception;

public class MuonTransportFailureException extends MuonException {
    public MuonTransportFailureException(String message) {
      super(message);
    }

    public MuonTransportFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public MuonTransportFailureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
