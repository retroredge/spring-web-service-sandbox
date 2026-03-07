package uk.co.redsoft.sandbox.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.Map;

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IOException.class)
    ResponseEntity<Map<String, String>> handleIOException(IOException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "Could not read uploaded file"));
    }
}
