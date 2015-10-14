package io.muoncore.exception;

public class NoSuchServiceException extends MuonException {

    private String serviceName;

    public NoSuchServiceException(String serviceName) {
        super("No service " + serviceName + " could be found");
        this.serviceName = serviceName;
    }

    public NoSuchServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchServiceException(Throwable cause) {
        super(cause);
    }

    public NoSuchServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public String getServiceName() {
        return serviceName;
    }
}
