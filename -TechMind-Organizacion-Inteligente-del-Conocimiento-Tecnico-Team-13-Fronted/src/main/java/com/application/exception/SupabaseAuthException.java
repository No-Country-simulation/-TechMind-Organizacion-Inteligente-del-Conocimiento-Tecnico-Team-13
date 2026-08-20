package com.application.exception;

public class SupabaseAuthException extends RuntimeException {
    public SupabaseAuthException(String message) {
        super(message);
    }
}
