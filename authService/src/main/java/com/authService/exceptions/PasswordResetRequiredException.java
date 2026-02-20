package com.authService.exceptions;

public class PasswordResetRequiredException extends RuntimeException {
    public PasswordResetRequiredException(String message) { super(message); }
}
