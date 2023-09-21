package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class BankExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> handlerAccountNotFoundException(AccountNotFoundException message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handlerException(Exception message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message.getMessage());
    }

    @ExceptionHandler(value = {IncorrectPinCodeException.class, IncorrectAccountNumberException.class, InsufficientFundsException.class})
    public ResponseEntity<Object> handleTransactionExceptions(RuntimeException message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message.getMessage());
    }
}
