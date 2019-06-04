package com.cisco.dnaspaces.exceptions;

public class FireHoseAPIException extends Exception {
    private int statusCode;

    public FireHoseAPIException() {
        super();
    }

    public FireHoseAPIException(String message) {
        super(message);
    }

    public FireHoseAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    public FireHoseAPIException(Throwable cause) {
        super(cause);
    }

    protected FireHoseAPIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public FireHoseAPIException(int statusCode) {
        super();
    }

    public FireHoseAPIException(String message, int statusCode) {
        super(message);
    }

    public FireHoseAPIException(String message, Throwable cause, int statusCode) {
        super(message, cause);
    }

    public FireHoseAPIException(Throwable cause, int statusCode) {
        super(cause);
    }

    protected FireHoseAPIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int statusCode) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public int getStatusCode() {
        return statusCode;
    }

}
