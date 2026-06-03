package com.wotos.wotosplayerservice.exception;

import feign.FeignException;
import feign.RetryableException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Centralizes exception handling across all controllers so that error responses share the
 * {@link ErrorResponse} schema ({@code {"error":{"code","message","correlationId"}}}) and
 * consistent HTTP status codes, keeping controllers free of per-endpoint try/catch blocks.
 *
 * <p>Every handled error is assigned a correlation id that is both returned to the caller and
 * logged, so a client-reported failure can be traced to its log line.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maps a missing domain entity to {@code 404 Not Found}.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
    }

    /**
     * Maps bean-validation failures on request bodies to {@code 400 Bad Request}, summarizing
     * each rejected field.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (message.isEmpty()) {
            message = "Validation failed";
        }

        return build(HttpStatus.BAD_REQUEST, message, ex);
    }

    /**
     * Maps bean-validation failures on request parameters (controllers annotated with
     * {@code @Validated}, e.g. {@code @RequestParam} + {@code @Max}/{@code @Language}) to
     * {@code 400 Bad Request}, summarizing each violated constraint.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));

        if (message.isEmpty()) {
            message = "Validation failed";
        }

        return build(HttpStatus.BAD_REQUEST, message, ex);
    }

    /**
     * Maps a downstream Wargaming/WoT API connect/read timeout to {@code 504 Gateway Timeout}.
     * Feign surfaces socket timeouts as {@link RetryableException}; handling it ahead of the
     * generic {@link FeignException} keeps timeouts distinguishable from other upstream errors.
     */
    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<ErrorResponse> handleTimeout(RetryableException ex) {
        return build(HttpStatus.GATEWAY_TIMEOUT, "Upstream WoT API timed out", ex);
    }

    /**
     * Maps failures calling the downstream Wargaming/WoT API to {@code 502 Bad Gateway}.
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeign(FeignException ex) {
        return build(HttpStatus.BAD_GATEWAY, "Upstream WoT API call failed: " + ex.getMessage(), ex);
    }

    /**
     * Catch-all for unhandled exceptions, mapped to {@code 500 Internal Server Error}. The raw
     * message is intentionally not leaked to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", ex);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, Exception ex) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        try {
            log.error("{} -> {} {}", ex.getClass().getSimpleName(), status.value(), message, ex);
        } finally {
            MDC.remove("correlationId");
        }
        return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), message, correlationId));
    }
}
