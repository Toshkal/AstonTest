package org.example.exception;

public class IncorrectPinCodeException extends RuntimeException {
    public IncorrectPinCodeException(String message) {
        super(message);
    }
}
