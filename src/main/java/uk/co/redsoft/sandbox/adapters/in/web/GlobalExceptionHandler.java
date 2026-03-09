package uk.co.redsoft.sandbox.adapters.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.co.redsoft.sandbox.domain.model.BookAlreadyExistsException;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BookAlreadyExistsException.class)
    ResponseEntity<Map<String, String>> handleBookAlreadyExists(BookAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IOException.class)
    ResponseEntity<Map<String, String>> handleIOException(IOException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "Could not read uploaded file"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        e -> e.getField(),
                        e -> e.getDefaultMessage(),
                        (a, b) -> a));
        return ResponseEntity.badRequest().body(errors);
    }
}
