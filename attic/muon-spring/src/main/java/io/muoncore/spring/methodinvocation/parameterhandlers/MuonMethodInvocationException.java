package io.muoncore.spring.methodinvocation.parameterhandlers;

import io.muoncore.exception.MuonException;

public class MuonMethodInvocationException extends MuonException {
    public MuonMethodInvocationException() {
        super();
    }

    public MuonMethodInvocationException(String message) {
        super(message);
    }

    public MuonMethodInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MuonMethodInvocationException(Throwable cause) {
        super(cause);
    }

    protected MuonMethodInvocationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
