package com.application.exception;

public class SupabaseRateLimitException extends RuntimeException {
    public SupabaseRateLimitException(String message) {
        super(message);
    }
}