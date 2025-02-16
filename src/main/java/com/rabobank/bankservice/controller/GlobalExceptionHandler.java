package com.rabobank.bankservice.controller;

import com.rabobank.bankservice.error.AccountNotFoundException;
import com.rabobank.bankservice.error.InsufficientBalanceException;
import com.rabobank.bankservice.error.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(AccountNotFoundException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientBalanceException(InsufficientBalanceException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Insufficient balance");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedException(UnauthorizedException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now());
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", "Not Authorized");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
}
