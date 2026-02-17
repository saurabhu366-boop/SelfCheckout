package com.sourabh.selfcheckout.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {
        String message = "barcode and userId are required";
        if (ex.getBindingResult().getFieldError() != null) {
            String field = ex.getBindingResult().getFieldError().getField();
            String msg = ex.getBindingResult().getFieldError().getDefaultMessage();
            message = (msg != null ? msg : field + " is invalid");
        }
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handle(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Bad request";
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }
}

