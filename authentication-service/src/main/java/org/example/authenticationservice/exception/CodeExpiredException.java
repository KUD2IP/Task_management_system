package org.example.authenticationservice.exception;

public class CodeExpiredException extends RuntimeException {
    public CodeExpiredException(String message) {
        super(message);
    }
}
