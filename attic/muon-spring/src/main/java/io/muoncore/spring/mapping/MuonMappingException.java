package io.muoncore.spring.mapping;

import io.muoncore.exception.MuonException;

public class MuonMappingException extends MuonException {
    public MuonMappingException() {
        super();
    }

    public MuonMappingException(String message) {
        super(message);
    }

    public MuonMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MuonMappingException(Throwable cause) {
        super(cause);
    }

    protected MuonMappingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
