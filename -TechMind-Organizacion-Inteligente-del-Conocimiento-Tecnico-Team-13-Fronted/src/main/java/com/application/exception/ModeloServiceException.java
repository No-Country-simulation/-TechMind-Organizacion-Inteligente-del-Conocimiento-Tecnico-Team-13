package com.application.exception;

public class ModeloServiceException extends RuntimeException {
    public ModeloServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModeloServiceException(String message) {
        super(message);
    }
}
