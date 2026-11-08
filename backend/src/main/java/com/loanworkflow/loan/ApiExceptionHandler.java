package com.loanworkflow.loan;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.loanworkflow.approval.ApprovalException;

/** Turns domain errors into HTTP answers with a short message. */
@RestControllerAdvice
public class ApiExceptionHandler {

    // Bad input: missing fields, negative numbers, unknown method names
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, e);
    }

    // The request is fine, but the loan is not in a state that allows it
    @ExceptionHandler({InvalidTransitionException.class, ApprovalException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> conflict(RuntimeException e) {
        return error(HttpStatus.CONFLICT, e);
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, RuntimeException e) {
        return ResponseEntity.status(status).body(Map.of("error", String.valueOf(e.getMessage())));
    }
}
